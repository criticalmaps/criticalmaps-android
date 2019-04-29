package de.stephanlindauer.criticalmaps.handler;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import javax.inject.Inject;

import de.stephanlindauer.criticalmaps.App;
import de.stephanlindauer.criticalmaps.R;
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

    public void showIntroductionIfNotShownBefore() {
        final BooleanPreference introductionAlreadyShownPreference = new BooleanPreference(
                sharedPreferences, SharedPrefsKeys.INTRODUCTION_ALREADY_SHOWN);

        if (introductionAlreadyShownPreference.isSet() &&
                introductionAlreadyShownPreference.get()) {
            return;
        }

        LayoutInflater factory = LayoutInflater.from(activity);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.AlertDialogTheme);

        @SuppressLint("InflateParams") // okay for dialog
        final View view = factory.inflate(R.layout.view_introduction, null);

        TextView textViewYou = view.findViewById(R.id.introduction_paragraph_you);
        Drawable mapMarkerOwn = AppCompatResources.getDrawable(activity, R.drawable.ic_map_marker_own);
        textViewYou.setCompoundDrawablesWithIntrinsicBounds(null, null, mapMarkerOwn, null);

        TextView textViewOthers = view.findViewById(R.id.introduction_paragraph_others);
        Drawable mapMarker = AppCompatResources.getDrawable(activity, R.drawable.ic_map_marker);
        textViewOthers.setCompoundDrawablesWithIntrinsicBounds(null, null, mapMarker, null);

        builder.setView(view);

        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    introductionAlreadyShownPreference.set(true);
                    break;
            }
        };

        builder.setPositiveButton(R.string.ok, dialogClickListener);
        builder.setCancelable(false);
        builder.show();
    }
}
