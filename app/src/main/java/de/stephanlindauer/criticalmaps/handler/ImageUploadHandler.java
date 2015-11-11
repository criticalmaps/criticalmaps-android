package de.stephanlindauer.criticalmaps.handler;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import de.stephanlindauer.criticalmaps.App;
import de.stephanlindauer.criticalmaps.AppConstants;
import de.stephanlindauer.criticalmaps.R;
import de.stephanlindauer.criticalmaps.model.OwnLocationModel;
import de.stephanlindauer.criticalmaps.utils.AlertBuilder;
import de.stephanlindauer.criticalmaps.vo.ResultType;

public class ImageUploadHandler extends AsyncTask<Void, Integer, ResultType> {
    private OwnLocationModel ownLocationModel = App.components().ownLocationmodel();

    private final Activity activity;
    private final File imageFileToUpload;
    private int totalAmountBytesToUpload;
    private ProgressDialog progressDialog;

    public ImageUploadHandler(File imageFileToUpload, Activity activity) {
        this.activity = activity;
        this.imageFileToUpload = imageFileToUpload;
    }

    @Override
    protected void onPreExecute() {
        progressDialog = new ProgressDialog(activity);
        progressDialog.setMessage(activity.getString(R.string.camera_uploading_progress));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setIndeterminate(false);
        progressDialog.setCancelable(false);
        progressDialog.setMax(100);
        progressDialog.setProgress(0);
        progressDialog.show();
    }

    @Override
    protected ResultType doInBackground(Void... params) {
        try {
            final String lineEnd = "\r\n";
            final String twoHyphens = "--";
            final String boundary = "*****";
            final int maxBufferSize = 32 * 1024;

            int bytesRead, bytesAvailable, bufferSize;

            FileInputStream fileInputStream = new FileInputStream(imageFileToUpload);
            URL url = new URL(AppConstants.IMAGE_POST);

            final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);

            connection.setRequestMethod("POST");

            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("ENCTYPE", "multipart/form-data");
            connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            connection.setRequestProperty("uploaded_file", imageFileToUpload.getName());

            DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());

            dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
            dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"" + "data" + "\"" + lineEnd);
            dataOutputStream.writeBytes("Content-Type: text/plain" + lineEnd);
            dataOutputStream.writeBytes(lineEnd);
            dataOutputStream.writeBytes(ownLocationModel.getLocationJson().toString());
            dataOutputStream.writeBytes(lineEnd);

            dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
            dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                    + imageFileToUpload.getName() + "\"" + lineEnd);

            dataOutputStream.writeBytes(lineEnd);

            bytesAvailable = fileInputStream.available();
            totalAmountBytesToUpload = bytesAvailable;

            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            byte[] buffer = new byte[bufferSize];

            bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            while (bytesRead > 0) {
                dataOutputStream.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                publishProgress(bytesAvailable);
            }

            dataOutputStream.writeBytes(lineEnd);
            dataOutputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            int serverResponseCode = connection.getResponseCode();

            if (serverResponseCode != 200) {
                throw new Exception();
            }

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            StringBuilder stringBuilder = new StringBuilder();
            String responseString;
            while ((responseString = bufferedReader.readLine()) != null) {
                stringBuilder.append(responseString);
            }

            if (!stringBuilder.toString().equals("success")) {
                return ResultType.FAILED;
            }

            fileInputStream.close();
            dataOutputStream.flush();
            dataOutputStream.close();
        } catch (Exception e) {
            return ResultType.FAILED;
        }

        return ResultType.SUCCEEDED;
    }

    @Override
    protected void onProgressUpdate(Integer... stillLeftToUpload) {
        int alreadyUploaded = totalAmountBytesToUpload - stillLeftToUpload[0];

        int onePercent = totalAmountBytesToUpload / 100;
        int percentUploaded = alreadyUploaded / onePercent;

        progressDialog.setProgress(percentUploaded);
    }

    @Override
    protected void onPostExecute(ResultType resultType) {
        if (resultType == ResultType.SUCCEEDED) {
            progressDialog.dismiss();
            AlertBuilder.show(activity, R.string.camera_image_upload_succeeded_title, R.string.camera_image_upload_succeeded_message);
        } else {
            progressDialog.dismiss();
            AlertBuilder.show(activity, R.string.camera_upload_failed_title, R.string.camera_upload_failed_message);
        }
        imageFileToUpload.delete();
    }
}
