package de.stephanlindauer.criticalmaps.handler;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.core.content.FileProvider;

import java.io.File;

import de.stephanlindauer.criticalmaps.App;
import de.stephanlindauer.criticalmaps.Main;
import de.stephanlindauer.criticalmaps.R;
import de.stephanlindauer.criticalmaps.utils.AlertBuilder;
import de.stephanlindauer.criticalmaps.utils.ImageUtils;
import de.stephanlindauer.criticalmaps.vo.RequestCodes;

public class StartCameraHandler {
    private final Main activity;

    public StartCameraHandler(Main mainActivity) {
        this.activity = mainActivity;
    }

    public void execute() {
        if (App.components().ownLocationmodel().ownLocation == null) {
            AlertBuilder.show(activity, R.string.something_went_wrong, R.string.camera_no_location);
            return;
        }

        final File outputFile = ImageUtils.getNewCacheImageFile();
        if (outputFile == null) {
            AlertBuilder.show(activity, R.string.something_went_wrong, R.string.camera_no_outputfile);
            return;
        }
        activity.setNewCameraOutputFile(Uri.fromFile(outputFile));

        Uri imageCaptureUri = FileProvider.getUriForFile(
                activity,
                activity.getResources().getString(R.string.fileprovider_authority),
                outputFile);

        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageCaptureUri);

        try {
            activity.startActivityForResult(
                    cameraIntent, RequestCodes.CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            AlertBuilder.show(activity, R.string.something_went_wrong, R.string.camera_no_camera);
        }
    }
}
