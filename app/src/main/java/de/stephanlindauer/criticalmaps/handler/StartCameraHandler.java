package de.stephanlindauer.criticalmaps.handler;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.MediaStore;

import de.stephanlindauer.criticalmaps.App;
import de.stephanlindauer.criticalmaps.Main;
import de.stephanlindauer.criticalmaps.R;
import de.stephanlindauer.criticalmaps.utils.AlertBuilder;
import de.stephanlindauer.criticalmaps.utils.ImageUtils;
import de.stephanlindauer.criticalmaps.vo.RequestCodes;

import java.io.File;

public class StartCameraHandler {

    private final Main activity;

    public StartCameraHandler(Main mainActivity) {
        this.activity = mainActivity;
    }

    public void execute() {
        if (!activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            AlertBuilder.show(activity, R.string.something_went_wrong, R.string.camera_no_camera);
            return;
        }

        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(activity.getPackageManager()) == null) {
            // TODO: 03.02.2018 more specific msg indicating there's no app to handle this intent
            AlertBuilder.show(activity, R.string.something_went_wrong, R.string.camera_no_camera);
            return;
        }

        if (App.components().ownLocationmodel().ownLocation == null) {
            AlertBuilder.show(activity, R.string.something_went_wrong, R.string.camera_no_location);
            return;
        }

        final File outputFile = ImageUtils.getNewOutputImageFile();
        if(outputFile == null) {
            AlertBuilder.show(activity, R.string.something_went_wrong, R.string.camera_no_outputfile);
            return;
        }
        activity.setNewCameraOutputFile(outputFile); // FIXME: 07.09.2015

        Uri imageCaptureUri = Uri.fromFile(outputFile);

        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageCaptureUri);
        activity.startActivityForResult(cameraIntent, RequestCodes.CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
    }
}
