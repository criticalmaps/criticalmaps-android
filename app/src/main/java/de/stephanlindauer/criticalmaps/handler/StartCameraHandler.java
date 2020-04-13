package de.stephanlindauer.criticalmaps.handler;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
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

    @SuppressLint("UnsupportedChromeOsCameraSystemFeature")
    public void execute() {
        boolean deviceHasCamera;
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN) {
            deviceHasCamera =
                    activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
        } else {
            deviceHasCamera =
                    activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
        }

        if (!deviceHasCamera) {
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

        // Workaround for permission issue on older devices, see:
        // https://medium.com/@quiro91/sharing-files-through-intents-part-2-fixing-the-permissions-before-lollipop-ceb9bb0eec3a
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            cameraIntent.setClipData(ClipData.newRawUri("", imageCaptureUri));
            cameraIntent.addFlags(
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }

        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageCaptureUri);
        activity.startActivityForResult(
                cameraIntent, RequestCodes.CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
    }
}
