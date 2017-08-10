package de.stephanlindauer.criticalmaps.handler;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

import javax.inject.Inject;

import de.stephanlindauer.criticalmaps.App;
import de.stephanlindauer.criticalmaps.R;
import de.stephanlindauer.criticalmaps.utils.ApplicationCloser;
import de.stephanlindauer.criticalmaps.prefs.SharedPrefsKeys;
import info.metadude.android.typedpreferences.BooleanPreference;

public class PrerequisitesChecker {

    @Inject
    SharedPreferences sharedPreferences;

    private final Activity activity;

    public PrerequisitesChecker(Activity activity) {
        this.activity = activity;
        App.components().inject(this);
    }

    public void execute() {
        if (!checkForLocationProvider()) {
            return;
        }

        showIntroductionIfNotShownBefore();
    }

    private boolean checkForLocationProvider() {
        LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            showAlertDialog(
                    activity,
                    R.string.prerequisites_no_gps_provider_enabled_title,
                    R.string.prerequisites_no_gps_provider_enabled_text,
                    Settings.ACTION_LOCATION_SOURCE_SETTINGS
            );
            return false;
        } else {
            return true;
        }
    }

    private boolean showIntroductionIfNotShownBefore() {
        final BooleanPreference introductionAlreadyShownPreference = new BooleanPreference(
                sharedPreferences, SharedPrefsKeys.INTRODUCTION_ALREADY_SHOWN);

        if (introductionAlreadyShownPreference.isSet() &&
                introductionAlreadyShownPreference.get()) {
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
                        introductionAlreadyShownPreference.set(true);
                        break;
                }
            }
        };

        builder.setPositiveButton(R.string.ok, dialogClickListener);
        builder.setCancelable(false);
        builder.show();

        return false;
    }

    private void showAlertDialog(final Activity activity,
                                 @StringRes int title,
                                 @StringRes int message,
                                 @NonNull final String intentAction) {
        new AlertDialog.Builder(activity)
                .setTitle(activity.getString(title))
                .setMessage(activity.getString(message))
                .setCancelable(false)
                .setPositiveButton(activity.getString(R.string.go_to_settings),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent viewIntent = createIntent(intentAction);
                                activity.startActivity(viewIntent);
                                ApplicationCloser.close(activity);
                            }
                        })
                .create()
                .show();
    }

    private Intent createIntent(String action) {
        Intent viewIntent = new Intent(action);
        viewIntent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK |
                            Intent.FLAG_ACTIVITY_NO_HISTORY |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK |
                            Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS |
                            Intent.FLAG_ACTIVITY_FORWARD_RESULT);
        return viewIntent;
    }
}
