package de.stephanlindauer.criticalmaps.handler;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;

import de.stephanlindauer.criticalmaps.App;
import de.stephanlindauer.criticalmaps.R;
import de.stephanlindauer.criticalmaps.utils.ImageUtils;
import de.stephanlindauer.criticalmaps.vo.ResultType;
import timber.log.Timber;

public class ProcessCameraResultHandler extends AsyncTask<Void, Void, ResultType> {

    private final Activity activity;
    private final File newCameraOutputFile;
    private File processedImageFile;
    private ProgressDialog progressDialog;
    private final Picasso picasso;

    public ProcessCameraResultHandler(Activity activity, File newCameraOutputFile) {
        this.activity = activity;
        this.newCameraOutputFile = newCameraOutputFile;
        this.picasso = App.components().picasso();
    }

    @Override
    protected void onPreExecute() {
        progressDialog = new ProgressDialog(activity);
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage(activity.getString(R.string.camera_processing_image));
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    @Override
    protected ResultType doInBackground(Void... params) {
        try {
            Bitmap processedBitmap = picasso.load(newCameraOutputFile)
                    .resize(1024, 1024)
                    .centerInside()
                    .get();

            processedImageFile = ImageUtils.getNewCacheImageFile();
            FileOutputStream fOut = new FileOutputStream(processedImageFile);
            processedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, fOut);
            fOut.flush();
            fOut.close();
            processedBitmap.recycle();
        } catch (Exception e) {
            Timber.d(e);
            return ResultType.FAILED;
        }

        return ResultType.SUCCEEDED;
    }

    @Override
    protected void onPostExecute(ResultType resultType) {
        progressDialog.dismiss();

        if (resultType == ResultType.FAILED) {
            Toast.makeText(activity, R.string.something_went_wrong, Toast.LENGTH_LONG).show();
            return;
        }

        LayoutInflater factory = LayoutInflater.from(activity);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        @SuppressLint("InflateParams") // okay for dialog
        final View view = factory.inflate(R.layout.view_picture_upload, null);

        ImageView image = view.findViewById(R.id.picture_preview);
        ViewGroup.LayoutParams layoutParams = image.getLayoutParams();
        picasso.load(processedImageFile)
                .resize(layoutParams.width, layoutParams.height)
                .centerInside()
                .into(image);

        TextView text = view.findViewById(R.id.picture_confirm_text);
        text.setMovementMethod(LinkMovementMethod.getInstance());
        text.setText(Html.fromHtml(activity.getString(R.string.camera_confirm_image_upload)));

        builder.setView(view);

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        new ImageUploadHandler(processedImageFile, activity).execute();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        //noinspection ResultOfMethodCallIgnored
                        processedImageFile.delete();
                        break;
                }
            }
        };

        builder.setPositiveButton(R.string.camera_upload, dialogClickListener);
        builder.setNegativeButton(R.string.camera_discard, dialogClickListener);
        builder.setCancelable(false);
        builder.show();
    }
}
