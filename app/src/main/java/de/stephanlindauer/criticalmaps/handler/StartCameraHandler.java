package de.stephanlindauer.criticalmaps.handler;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.widget.Toast;

import java.io.File;

import de.stephanlindauer.criticalmaps.R;
import de.stephanlindauer.criticalmaps.fragments.SuperFragment;
import de.stephanlindauer.criticalmaps.utils.ImageUtils;
import de.stephanlindauer.criticalmaps.vo.chat.RequestCodes;
import de.stephanlindauer.criticalmaps.vo.chat.ResultType;

public class StartCameraHandler extends AsyncTask<Void, Void, ResultType> {

    private SuperFragment superFragment;
    private File outputFile;
    private Context context;

    public StartCameraHandler(SuperFragment superFragment) {
        this.superFragment = superFragment;
    }

    @Override
    protected ResultType doInBackground(Void... voids) {
        outputFile = ImageUtils.getNewOutputImageFile();
        superFragment.setNewCameraOutputFile(outputFile);

        context = superFragment.getActivity();
        PackageManager packageManager = context.getPackageManager();

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
        if (resultType == ResultType.FAILED){
            Toast.makeText(context, R.string.no_camera, Toast.LENGTH_LONG).show();
        }
    }
}
