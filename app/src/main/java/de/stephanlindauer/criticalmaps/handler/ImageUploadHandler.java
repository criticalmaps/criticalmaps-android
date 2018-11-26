package de.stephanlindauer.criticalmaps.handler;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;

import org.jetbrains.annotations.NotNull;

import de.stephanlindauer.criticalmaps.App;
import de.stephanlindauer.criticalmaps.R;
import de.stephanlindauer.criticalmaps.model.OwnLocationModel;
import de.stephanlindauer.criticalmaps.utils.AlertBuilder;
import de.stephanlindauer.criticalmaps.vo.Endpoints;
import de.stephanlindauer.criticalmaps.vo.ResultType;
import java.io.File;
import java.io.IOException;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;


public class ImageUploadHandler extends AsyncTask<Void, Integer, ResultType> {

    private final OwnLocationModel ownLocationModel = App.components().ownLocationmodel();

    private final Activity activity;
    private final File imageFileToUpload;
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
        progressDialog.setProgressNumberFormat(null);
        progressDialog.setMax(100);
        progressDialog.setProgress(0);
        progressDialog.show();
    }

    @Override
    protected ResultType doInBackground(Void... params) {

        final OkHttpClient okHttpClient = App.components().okHttpClient();

        final ProgressListener progressListener = new ProgressListener() {
            @Override
            public void update(long bytesRead, long contentLength) {
                publishProgress((int) ((100 * bytesRead) / contentLength));
            }
        };

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("data", ownLocationModel.getLocationJson().toString())
                .addFormDataPart("uploaded_file", imageFileToUpload.getName(),
                        new ProgressRequestBody(
                                RequestBody.create(MediaType.parse("image/jpeg"), imageFileToUpload),
                                progressListener))
                .build();

        Request request = new Request.Builder().url(Endpoints.IMAGE_POST).post(requestBody).build();

        try {
            Response response = okHttpClient.newCall(request).execute();

            //noinspection ConstantConditions "Returns a non-null value if this response was [...] returned from Call.execute()."
            if (response.isSuccessful() && response.body().string().equals("success")) {
                    return ResultType.SUCCEEDED;
            }
        } catch (Exception ignored) {
        }

        return ResultType.FAILED;
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        progressDialog.setProgress(progress[0]);
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
        //noinspection ResultOfMethodCallIgnored
        imageFileToUpload.delete();
    }

    private static class ProgressRequestBody extends RequestBody {
        private final RequestBody requestBody;
        private final ProgressListener progressListener;

        public ProgressRequestBody(RequestBody requestBody, ProgressListener progressListener) {
            this.requestBody = requestBody;
            this.progressListener = progressListener;
        }

        @Override
        public MediaType contentType() {
            return requestBody.contentType();
        }

        @Override
        public long contentLength() throws IOException {
            return requestBody.contentLength();
        }

        @Override
        public void writeTo(@NotNull BufferedSink sink) throws IOException {
            final long totalBytes = contentLength();
            BufferedSink progressSink = Okio.buffer(new ForwardingSink(sink) {
                private long bytesWritten = 0L;

                @Override
                public void write(@NotNull Buffer source, long byteCount) throws IOException {
                    bytesWritten += byteCount;
                    progressListener.update(bytesWritten, totalBytes);
                    super.write(source, byteCount);
                }
            });
            requestBody.writeTo(progressSink);
            progressSink.flush();
        }
    }

    private interface ProgressListener {
        void update(long bytesRead, long contentLength);
    }
}
