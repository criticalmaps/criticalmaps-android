package de.stephanlindauer.criticalmaps.handler;

import android.content.SharedPreferences;
import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import javax.inject.Inject;

import de.stephanlindauer.criticalmaps.BuildConfig;
import de.stephanlindauer.criticalmaps.managers.LocationUpdateManager;
import de.stephanlindauer.criticalmaps.model.ChatModel;
import de.stephanlindauer.criticalmaps.model.OwnLocationModel;
import de.stephanlindauer.criticalmaps.model.UserModel;
import de.stephanlindauer.criticalmaps.prefs.SharedPrefsKeys;
import de.stephanlindauer.criticalmaps.vo.Endpoints;
import info.metadude.android.typedpreferences.BooleanPreference;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import timber.log.Timber;

public class GetLocationHandler extends AsyncTask<Void, Void, String> {

    //dependencies
    private final ChatModel chatModel;
    private final OwnLocationModel ownLocationModel;
    private final UserModel userModel;
    private final OkHttpClient okHttpClient;
    private final SharedPreferences sharedPreferences;
    private final LocationUpdateManager locationUpdateManager;
    private final ServerResponseProcessor serverResponseProcessor;

    @Inject
    public GetLocationHandler(ChatModel chatModel,
                              OwnLocationModel ownLocationModel,
                              UserModel userModel,
                              ServerResponseProcessor serverResponseProcessor,
                              OkHttpClient okHttpClient,
                              SharedPreferences sharedPreferences,
                              LocationUpdateManager locationUpdateManager) {
        this.chatModel = chatModel;
        this.ownLocationModel = ownLocationModel;
        this.userModel = userModel;
        this.okHttpClient = okHttpClient;
        this.sharedPreferences = sharedPreferences;
        this.locationUpdateManager = locationUpdateManager;
        this.serverResponseProcessor = serverResponseProcessor;
    }

    @Override
    protected String doInBackground(Void... params) {
        final Headers headers = Headers.of("app-version", BuildConfig.VERSION_NAME);

        final Request request = new Request.Builder().url(Endpoints.LOCATION_GET).get().headers(headers).build();

        try {
            final Response response = okHttpClient.newCall(request).execute();
            if (!response.isSuccessful()) {
                Timber.d("Get locations unsuccessful.");
            }
            return response.body().string();
        } catch (IOException e) {
            Timber.e(e);
        }
        return "";
    }

    @Override
    protected void onPostExecute(String result) {
        if (!result.isEmpty()) {
            serverResponseProcessor.process(result);
        }
    }
}
