package de.stephanlindauer.criticalmaps.handler;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;

import de.stephanlindauer.criticalmaps.App;
import de.stephanlindauer.criticalmaps.Main;
import de.stephanlindauer.criticalmaps.R;
import de.stephanlindauer.criticalmaps.utils.AlertBuilder;
import de.stephanlindauer.criticalmaps.utils.ImageUtils;
import de.stephanlindauer.criticalmaps.vo.RequestCodes;
import de.stephanlindauer.criticalmaps.vo.ResultType;

import java.io.File;

public class StartCameraHandler extends AsyncTask<Void, Void, ResultType> {

    private final Main activity;

    public StartCameraHandler(Main mainActivity) {
        this.activity = mainActivity;
    }

    @Override
    protected void onPreExecute() {
        if (!activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            AlertBuilder.show(activity,R.string.something_went_wrong,R.string.camera_no_camera_no_camera);
            cancel(true);
        }
        else if (App.components().ownLocationmodel().ownLocation == null) {
            AlertBuilder.show(activity,R.string.something_went_wrong,R.string.camera_no_location_no_camera);
            cancel(true);
        }
    }

    @Override
    protected ResultType doInBackground(Void... voids) {
        final File outputFile = ImageUtils.getNewOutputImageFile();
        activity.setNewCameraOutputFile(outputFile); // FIXME: 07.09.2015

        PackageManager packageManager = activity.getPackageManager();

        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return ResultType.FAILED;
        }

        Uri imageCaptureUri = Uri.fromFile(outputFile);

        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageCaptureUri);
        activity.startActivityForResult(cameraIntent, RequestCodes.CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
        return ResultType.SUCCEEDED;
    }

    @Override
    protected void onPostExecute(ResultType resultType) {
        if (resultType == ResultType.FAILED) {
            AlertBuilder.show(activity,R.string.something_went_wrong,R.string.camera_no_camera);
        }
    }
}
