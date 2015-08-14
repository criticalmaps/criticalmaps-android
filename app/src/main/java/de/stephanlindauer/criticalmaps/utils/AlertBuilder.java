package de.stephanlindauer.criticalmaps.utils;

import android.app.Activity;
import android.app.AlertDialog;

import de.stephanlindauer.criticalmaps.R;

public class AlertBuilder {
    public static void show(Activity activity, int title, int text) {
        new AlertDialog.Builder(activity)
                .setTitle(activity.getString(title))
                .setMessage(activity.getString(text))
                .setCancelable(false)
                .setPositiveButton(activity.getString(R.string.ok), null)
                .create()
                .show();
    }
}
