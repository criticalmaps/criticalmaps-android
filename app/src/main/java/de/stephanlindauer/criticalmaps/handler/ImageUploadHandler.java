package de.stephanlindauer.criticalmaps.handler;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import de.stephanlindauer.criticalmaps.R;
import de.stephanlindauer.criticalmaps.vo.chat.Endpoints;
import de.stephanlindauer.criticalmaps.vo.chat.ResultType;

public class ImageUploadHandler extends AsyncTask<Void, Integer, ResultType> {
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
            FileInputStream imageFileInputStream = new FileInputStream(imageFileToUpload);

                     String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";

            int bytesRead;

            int bufferSize;

            byte[] buffer;

            int maxBufferSize = 1024 * 8;

            URL url = new URL(Endpoints.IMAGE_POST);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setChunkedStreamingMode(maxBufferSize);

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Cache-Control", "no-cache");
            connection.setRequestProperty("ENCTYPE", "multipart/form-data");
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
//            connection.setRequestProperty("uploaded_file", imageFileToUpload.getName());
//            connection.setRequestProperty("Content-Length", String.valueOf(imageFileToUpload.length()));

            DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());

            dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
            dataOutputStream.writeBytes("Content-Disposition: form-data;name=\"uploaded_file\";filename=\"" + imageFileToUpload.getName() + "\"" + lineEnd);
            dataOutputStream.writeBytes("Content-Type: image/jpeg" + lineEnd);

            dataOutputStream.writeBytes(lineEnd);

            totalAmountBytesToUpload = imageFileInputStream.available();

            bufferSize = Math.min(totalAmountBytesToUpload, maxBufferSize);
            buffer = new byte[bufferSize];

            bytesRead = imageFileInputStream.read(buffer, 0, bufferSize);

            while (bytesRead > 0) {
                dataOutputStream.write(buffer, 0, bufferSize);
                int restBytesToUpload = imageFileInputStream.available();

                bufferSize = Math.min(restBytesToUpload, maxBufferSize);
                bytesRead = imageFileInputStream.read(buffer, 0, bufferSize);

                publishProgress(restBytesToUpload);
            }

            dataOutputStream.writeBytes(lineEnd);
            dataOutputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            int serverResponseCode = connection.getResponseCode();
            String serverResponseMessage = connection.getResponseMessage();

            Log.i("uploadFile", "HTTP Response is : " + serverResponseMessage + ": " + serverResponseCode);

            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder sb = new StringBuilder();

            String responseString;
            while ((responseString = br.readLine()) != null) {
                sb.append(responseString);
            }

//            JSONObject responseObject = new JSONObject(sb.toString());

//            imageFileNameOnServer = responseObject.getString("image");

            imageFileInputStream.close();
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
//            eventService.post(new NewImageReceivedFromServer(new OfferImage(imageFileNameOnServer)));
        } else {
            progressDialog.dismiss();
            showErrorMessage();
        }
    }

    private void showErrorMessage() {
        new AlertDialog.Builder(activity)
                .setTitle(activity.getString(R.string.image_upload_failed_title))
                .setMessage(activity.getString(R.string.image_upload_failed_message))
                .setCancelable(false)
                .setPositiveButton(activity.getString(R.string.image_upload_failed_accept), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).create().show();
    }
}
