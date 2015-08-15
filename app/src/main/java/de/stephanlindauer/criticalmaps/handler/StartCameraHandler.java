package de.stephanlindauer.criticalmaps.handler;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;

import java.io.File;

import de.stephanlindauer.criticalmaps.R;
import de.stephanlindauer.criticalmaps.fragments.SuperFragment;
import de.stephanlindauer.criticalmaps.model.OwnLocationModel;
import de.stephanlindauer.criticalmaps.utils.ImageUtils;
import de.stephanlindauer.criticalmaps.vo.RequestCodes;
import de.stephanlindauer.criticalmaps.vo.ResultType;

public class StartCameraHandler extends AsyncTask<Void, Void, ResultType> {

    private SuperFragment superFragment;
    private File outputFile;
    private Activity activity;


    public StartCameraHandler(SuperFragment superFragment) {
        this.superFragment = superFragment;
        this.activity = superFragment.getActivity();
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
    }

    @Override
    protected ResultType doInBackground(Void... voids) {
        outputFile = ImageUtils.getNewOutputImageFile();
        superFragment.setNewCameraOutputFile(outputFile);

        PackageManager packageManager = activity.getPackageManager();

        if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA) == false) {
            return ResultType.FAILED;
        }

        Uri imageCaptureUri = Uri.fromFile(outputFile);

        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageCaptureUri);
        superFragment.startActivityForResult(cameraIntent, RequestCodes.CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
        return ResultType.SUCCEEDED;
    }

    @Override
    protected void onPostExecute(ResultType resultType) {
        if (resultType == ResultType.FAILED) {
            new AlertDialog.Builder(activity)
                    .setMessage(R.string.no_camera)
                    .setPositiveButton(R.string.ok, null)
                    .show();
        }
    }
}
