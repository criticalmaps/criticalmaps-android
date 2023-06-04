package de.stephanlindauer.criticalmaps.handler;

import android.os.AsyncTask;

import org.json.JSONObject;

import java.io.IOException;

import de.stephanlindauer.criticalmaps.App;
import de.stephanlindauer.criticalmaps.BuildConfig;
import de.stephanlindauer.criticalmaps.vo.Endpoints;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import timber.log.Timber;


public class PostChatmessagesHandler extends AsyncTask<Void, Void, Boolean> {

    private final OkHttpClient okHttpClient = App.components().okHttpClient();

    private final JSONObject message;
    private final Runnable onSuccessCallback;
    private final Runnable onErrorCallback;

    public PostChatmessagesHandler(JSONObject message, Runnable onSuccessCallback, Runnable onErrorCallback) {
        this.message = message;
        this.onSuccessCallback = onSuccessCallback;
        this.onErrorCallback = onErrorCallback;
    }

    @Override
    protected Boolean doInBackground(Void... params) {

        String jsonBody = message.toString();
        final RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonBody);
        final Headers headers = Headers.of("app-version", BuildConfig.VERSION_NAME);
        final Request request = new Request.Builder().url(Endpoints.CHAT_POST).post(body).headers(headers).build();

        boolean wasSuccessful = false;
        try {
            final Response response = okHttpClient.newCall(request).execute();
            if (!response.isSuccessful()) {
                Timber.d("Post chatmessages unsuccessful with code %d", response.code());
            }
            wasSuccessful = response.isSuccessful();
            response.close();
        } catch (IOException e) {
            Timber.e(e);
        }

        return wasSuccessful;
    }

    @Override
    protected void onPostExecute(Boolean success) {
        if (success) {
            onSuccessCallback.run();
        } else {
            onErrorCallback.run();
        }
    }
}
