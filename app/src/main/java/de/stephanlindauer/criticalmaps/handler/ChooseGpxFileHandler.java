package de.stephanlindauer.criticalmaps.handler;

import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresApi;

import de.stephanlindauer.criticalmaps.R;
import de.stephanlindauer.criticalmaps.fragments.SettingsFragment;
import de.stephanlindauer.criticalmaps.vo.RequestCodes;

public class ChooseGpxFileHandler {
    private final SettingsFragment activity;

    public ChooseGpxFileHandler(SettingsFragment activity) {
        this.activity = activity;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void openChooser() {
        Intent chooseTrack = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        chooseTrack.setType("*/*");
        chooseTrack = Intent.createChooser(chooseTrack, activity.getString(R.string.gpx_choose));
        activity.startActivityForResult(chooseTrack, RequestCodes.CHOOSE_GPX_RESULT_CODE);
    }
}
