package de.stephanlindauer.criticalmaps.commands;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class SnapshotUploadTask extends AsyncTask<Object, Integer, Void> {

    private final File file;
    private final ProgressDialog progressDialog;
    private final Activity activity;

    public SnapshotUploadTask(File file, ProgressDialog progressDialog, Activity activity) {
        this.progressDialog = progressDialog;
        this.file = file;
        this.activity = activity;
    }

    @Override
    protected void onPreExecute() {
        progressDialog.setProgress(0);
    }

    @Override
    protected Void doInBackground(Object... arg0) {

        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";

        int bytesRead;
        int bytesAvailable;
        int bufferSize;

        byte[] buffer;

//        int maxBufferSize = 1 * 1024 * 1024;
        int maxBufferSize = 1 * 512;

        try {

            FileInputStream fileInputStream = new FileInputStream(file);
            URL url = new URL("http://api.criticalmaps.net/pic.php");

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("ENCTYPE", "multipart/form-data");
            connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            connection.setRequestProperty("uploaded_file", file.getName());

            DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());

            dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
            dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\"" + file.getName() + "\"" + lineEnd);

            dataOutputStream.writeBytes(lineEnd);

            bytesAvailable = fileInputStream.available();
            final int hundredPercent = bytesAvailable;
            progressDialog.setMax(hundredPercent);
            progressDialog.setMax(35000);

            int onePercent = hundredPercent / 100;

            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

            bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            while (bytesRead > 0) {
                dataOutputStream.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                System.out.println();

                int restBytes = bytesAvailable;
                final int uploadedBytes = hundredPercent - restBytes;

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.setProgress((int) uploadedBytes);
                    }
                });

                Log.d("bla", "uploadedBytes  " + uploadedBytes);
                Log.d("bla", "#################################");
                Log.d("bla", "bufferSize     " + bufferSize);
                Log.d("bla", "bytesRead      " + bytesRead);
                Log.d("bla", "bytesAvailable " + bytesAvailable);
                Log.d("bla", "#################################");
                Log.d("bla", "-");
            }

            dataOutputStream.writeBytes(lineEnd);
            dataOutputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            int serverResponseCode = connection.getResponseCode();
            String serverResponseMessage = connection.getResponseMessage();

            Log.i("uploadFile", "HTTP Response is : " + serverResponseMessage + ": " + serverResponseCode);

            if (serverResponseCode == 200) {
                progressDialog.dismiss();
            }

            fileInputStream.close();
            dataOutputStream.flush();
            dataOutputStream.close();

        } catch (Exception e) {
            progressDialog.dismiss();

            Log.e("Upload file to server Exception", "Exception : " + e.getMessage(), e);
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        progressDialog.dismiss();
    }
}