package de.stephanlindauer.criticalmaps.handler;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;

import android.support.v7.app.AlertDialog;
import java.io.File;

import de.stephanlindauer.criticalmaps.Main;
import de.stephanlindauer.criticalmaps.R;
import de.stephanlindauer.criticalmaps.model.OwnLocationModel;
import de.stephanlindauer.criticalmaps.utils.ImageUtils;
import de.stephanlindauer.criticalmaps.vo.RequestCodes;
import de.stephanlindauer.criticalmaps.vo.ResultType;

public class StartCameraHandler extends AsyncTask<Void, Void, ResultType> {

    private File outputFile;
    private Activity activity;


    public StartCameraHandler(Activity mainActivity) {
        this.activity = mainActivity;
    }

    @Override
    protected void onPreExecute() {
        if (OwnLocationModel.getInstance().ownLocation == null) {
            new AlertDialog.Builder(activity)
                    .setMessage(R.string.camera_no_location_no_camera)
                    .setPositiveButton(R.string.ok, null)
                    .show();
            cancel(true);
        }
        if (!activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            new AlertDialog.Builder(activity)
                    .setMessage(R.string.camera_no_camera_no_camera)
                    .setPositiveButton(R.string.ok, null)
                    .show();
            cancel(true);
        }
    }

    @Override
    protected ResultType doInBackground(Void... voids) {
        outputFile = ImageUtils.getNewOutputImageFile();
        ((Main)activity).setNewCameraOutputFile(outputFile); // FIXME: 07.09.2015

        PackageManager packageManager = activity.getPackageManager();

        if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA) == false) {
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
            new AlertDialog.Builder(activity)
                    .setMessage(R.string.camera_no_camera)
                    .setPositiveButton(R.string.ok, null)
                    .show();
        }
    }
}
