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


public class GetLocationHandler extends AsyncTask<Void, Void, String> {

    private final OkHttpClient okHttpClient;
    private final ServerResponseProcessor serverResponseProcessor;

    @Inject
    public GetLocationHandler(ServerResponseProcessor serverResponseProcessor,
                              OkHttpClient okHttpClient
    ) {
        this.okHttpClient = okHttpClient;
        this.serverResponseProcessor = serverResponseProcessor;
    }

    @Override
    protected String doInBackground(Void... params) {
        final Headers headers = Headers.of("app-version", BuildConfig.VERSION_NAME);
        final Request request = new Request.Builder().url(Endpoints.LOCATION_GET).get().headers(headers).build();

        String responseString = "";
        try {
            final Response response = okHttpClient.newCall(request).execute();
            if (!response.isSuccessful()) {
                Timber.d("Get locations unsuccessful with code %d", response.code());
            }
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
            serverResponseProcessor.processLocations(result);
        }
    }
}
