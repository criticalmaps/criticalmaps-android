package de.stephanlindauer.criticalmaps.handler;

import android.os.AsyncTask;

import java.io.IOException;

import javax.inject.Inject;

import de.stephanlindauer.criticalmaps.BuildConfig;
import de.stephanlindauer.criticalmaps.vo.Endpoints;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;


public class GetChatmessagesHandler extends AsyncTask<Void, Void, String> {

    private final OkHttpClient okHttpClient;
    private final ServerResponseProcessor serverResponseProcessor;

    @Inject
    public GetChatmessagesHandler(ServerResponseProcessor serverResponseProcessor,
                                  OkHttpClient okHttpClient
    ) {
        this.okHttpClient = okHttpClient;
        this.serverResponseProcessor = serverResponseProcessor;
    }

    @Override
    protected String doInBackground(Void... params) {
        final Headers headers = Headers.of("app-version", BuildConfig.VERSION_NAME);
        final Request request = new Request.Builder().url(Endpoints.CHAT_GET).get().headers(headers).build();

        String responseString = "";
        try {
            final Response response = okHttpClient.newCall(request).execute();
            if (!response.isSuccessful()) {
                Timber.d("Get chatmessages unsuccessful with code %d", response.code());
            }
            //noinspection ConstantConditions "Returns a non-null value if this response was [...] returned from Call.execute()."
            responseString = response.body().string();
            response.body().close();
        } catch (IOException e) {
            Timber.e(e);
        }

        return responseString;
    }

    @Override
    protected void onPostExecute(String result) {
        if (!result.isEmpty()) {
            serverResponseProcessor.processChatmessages(result);
        }
    }
}
