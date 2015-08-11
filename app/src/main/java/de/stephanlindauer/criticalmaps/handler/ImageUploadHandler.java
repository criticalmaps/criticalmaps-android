package de.stephanlindauer.criticalmaps.handler;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
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
            HttpURLConnection conn = null;
            DataOutputStream dos = null;
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1 * 1024 * 1024;

            FileInputStream fileInputStream = new FileInputStream(imageFileToUpload);
            URL url = new URL(Endpoints.IMAGE_POST);

            conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);

            conn.setRequestMethod("POST");

            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("ENCTYPE", "multipart/form-data");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            conn.setRequestProperty("uploaded_file", imageFileToUpload.getName());

            dos = new DataOutputStream(conn.getOutputStream());

            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                    + imageFileToUpload.getName() + "\"" + lineEnd);

            dos.writeBytes(lineEnd);

            bytesAvailable = fileInputStream.available();
            totalAmountBytesToUpload = bytesAvailable;

            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

            bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            while (bytesRead > 0) {
                dos.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                publishProgress(bytesAvailable);
            }

            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            int serverResponseCode = conn.getResponseCode();
            String serverResponseMessage = conn.getResponseMessage();

            Log.i("uploadFile", "HTTP Response is : " + serverResponseMessage + ": " + serverResponseCode);

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();

            String responseString;
            while ((responseString = br.readLine()) != null) {
                sb.append(responseString);
            }

            fileInputStream.close();
            dos.flush();
            dos.close();
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
