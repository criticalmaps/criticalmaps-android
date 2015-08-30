package de.stephanlindauer.criticalmaps.handler;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;

import de.stephanlindauer.criticalmaps.R;
import de.stephanlindauer.criticalmaps.utils.ApplicationCloser;
import de.stephanlindauer.criticalmaps.vo.SharedPrefsKeys;

public class PrerequisitesChecker {
    private final Activity activity;

    public PrerequisitesChecker(Activity activity) {
        this.activity = activity;
    }

    public void execute() {
        if (!checkForLocationProvider()) {
            return;
        }
        if (!checkForInternetAvailable()) {
            return;
        }
        if (!checkForIntroductionShown()) {
            return;
        }
    }

    private boolean checkForLocationProvider() {
        LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            new AlertDialog.Builder(activity)
                    .setTitle(activity.getString(R.string.prerequisites_no_gps_provider_enabled_title))
                    .setMessage(activity.getString(R.string.prerequisites_no_gps_provider_enabled_text))
                    .setCancelable(false)
                    .setPositiveButton(activity.getString(R.string.go_to_settings),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent viewIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                    activity.startActivity(viewIntent);
                                    ApplicationCloser.close(activity);
                                }
                            })
                    .create()
                    .show();
            return false;
        } else {
            return true;
        }
    }

    private boolean checkForInternetAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        if (activeNetworkInfo == null || !activeNetworkInfo.isConnected()) {
            new AlertDialog.Builder(activity)
                    .setTitle(activity.getString(R.string.prerequisites_no_internet_enabled_title))
                    .setMessage(activity.getString(R.string.prerequisites_no_internet_enabled_text))
                    .setCancelable(false)
                    .setPositiveButton(activity.getString(R.string.go_to_settings),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent viewIntent = new Intent(android.provider.Settings.ACTION_DATA_ROAMING_SETTINGS);
                                    activity.startActivity(viewIntent);
                                    ApplicationCloser.close(activity);
                                }
                            })
                    .create()
                    .show();
            return false;
        } else {
            return true;
        }
    }

    private boolean checkForIntroductionShown() {
        final SharedPreferences sharedPreferences = activity.getPreferences(Context.MODE_PRIVATE);

        if (sharedPreferences.contains(SharedPrefsKeys.introductionAlreadyShown) && sharedPreferences.getBoolean(SharedPrefsKeys.introductionAlreadyShown, true)) {
            return true;
        }

        LayoutInflater factory = LayoutInflater.from(activity);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        final View view = factory.inflate(R.layout.view_introduction, null);
        builder.setView(view);

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        sharedPreferences
                                .edit()
                                .putBoolean(SharedPrefsKeys.introductionAlreadyShown, true)
                                .commit();
                        break;
                }
            }
        };

        builder.setPositiveButton(R.string.ok, dialogClickListener);
        builder.setCancelable(false);
        builder.show();

        return false;
    }

}
