/*
 * Copyright (C) 2019 Veli Tasalı
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package com.genonbeta.TrebleShot;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import com.genonbeta.TrebleShot.config.AppConfig;
import com.genonbeta.TrebleShot.config.Keyword;
import com.genonbeta.TrebleShot.object.NetworkDevice;
import com.genonbeta.TrebleShot.util.AppUtils;
import com.genonbeta.TrebleShot.util.PreferenceUtils;
import com.genonbeta.TrebleShot.util.UpdateUtils;
import com.genonbeta.android.framework.preference.DbSharablePreferences;
import com.genonbeta.android.updatewithgithub.GitHubUpdater;

import java.util.Locale;

/**
 * created by: Veli
 * date: 25.02.2018 01:23
 */

public class App extends Application
{
    public static final String TAG = App.class.getSimpleName();

    @Override
    public void onCreate()
    {
        super.onCreate();

        initializeSettings();

        if (!Keyword.Flavor.googlePlay.equals(AppUtils.getBuildFlavor())
                && !UpdateUtils.hasNewVersion(getApplicationContext())
                && (System.currentTimeMillis() - UpdateUtils.getLastTimeCheckedForUpdates(getApplicationContext())) >= AppConfig.DELAY_CHECK_FOR_UPDATES) {
            GitHubUpdater updater = UpdateUtils.getDefaultUpdater(getApplicationContext());
            UpdateUtils.checkForUpdates(getApplicationContext(), updater, false, null);
        }
    }

    private void initializeSettings()
    {
        //SharedPreferences defaultPreferences = AppUtils.getDefaultLocalPreferences(this);
        SharedPreferences defaultPreferences = AppUtils.getDefaultPreferences(this);
        NetworkDevice localDevice = AppUtils.getLocalDevice(getApplicationContext());
        boolean nsdDefined = defaultPreferences.contains("nsd_enabled");
        boolean refVersion = defaultPreferences.contains("referral_version");

        PreferenceManager.setDefaultValues(this, R.xml.preferences_defaults_main, false);

        if (!refVersion)
            defaultPreferences.edit()
                    .putInt("referral_version", localDevice.versionNumber)
                    .apply();

        // Some pre-kitkat devices were soft rebooting when this feature was turned on by default.
        // So we will disable it for them and they will still remain an option for the user.
        if (!nsdDefined)
            defaultPreferences.edit()
                    .putBoolean("nsd_enabled", Build.VERSION.SDK_INT >= 19)
                    .apply();

        if (defaultPreferences.contains("migrated_version")) {
            int migratedVersion = defaultPreferences.getInt("migrated_version", localDevice.versionNumber);

            if (migratedVersion < localDevice.versionNumber) {
                // migrating to a new version

                if (migratedVersion <= 67)
                    AppUtils.getViewingPreferences(getApplicationContext()).edit()
                            .clear()
                            .apply();

                defaultPreferences.edit()
                        .putInt("migrated_version", localDevice.versionNumber)
                        .putInt("previously_migrated_version", migratedVersion)
                        .apply();
            }
        } else
            defaultPreferences.edit()
                    .putInt("migrated_version", localDevice.versionNumber)
                    .apply();
    }
}
