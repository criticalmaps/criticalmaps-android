package de.stephanlindauer.criticalmaps.handler;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import de.stephanlindauer.criticalmaps.managers.LocationUpdateManager;
import de.stephanlindauer.criticalmaps.model.ChatModel;
import de.stephanlindauer.criticalmaps.model.OwnLocationModel;
import de.stephanlindauer.criticalmaps.model.UserModel;
import de.stephanlindauer.criticalmaps.prefs.SharedPrefsKeys;
import de.stephanlindauer.criticalmaps.vo.Endpoints;
import java.io.IOException;
import javax.inject.Inject;

import info.metadude.android.typedpreferences.BooleanPreference;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import timber.log.Timber;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PullServerHandler extends AsyncTask<Void, Void, String> {

    //dependencies
    private final ChatModel chatModel;
    private final OwnLocationModel ownLocationModel;
    private final UserModel userModel;
    private final ServerResponseProcessor serverResponseProcessor;
    private final OkHttpClient okHttpClient;
    private final SharedPreferences sharedPreferences;
    private final LocationUpdateManager locationUpdateManager;

    @Inject
    public PullServerHandler(ChatModel chatModel,
                             OwnLocationModel ownLocationModel,
                             UserModel userModel,
                             ServerResponseProcessor serverResponseProcessor,
                             OkHttpClient okHttpClient,
                             SharedPreferences sharedPreferences,
                             LocationUpdateManager locationUpdateManager) {
        this.chatModel = chatModel;
        this.ownLocationModel = ownLocationModel;
        this.userModel = userModel;
        this.serverResponseProcessor = serverResponseProcessor;
        this.okHttpClient = okHttpClient;
        this.sharedPreferences = sharedPreferences;
        this.locationUpdateManager = locationUpdateManager;
    }

    @Override
    protected String doInBackground(Void... params) {
        String jsonPostString = getJsonObject().toString();

        final RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonPostString);
        final Request request = new Request.Builder().url(Endpoints.MAIN_POST).post(body).build();

        try {
            final Response response = okHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                //noinspection ConstantConditions "Returns a non-null value if this response was [...] returned from Call.execute()."
                return response.body().string();
            }
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

    private JSONObject getJsonObject() {
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("device", userModel.getChangingDeviceToken());

            final boolean isObserverModeActive = new BooleanPreference(
                    sharedPreferences, SharedPrefsKeys.OBSERVER_MODE_ACTIVE).get();

            Timber.d("observer mode enabled: %s", isObserverModeActive);

            if (!isObserverModeActive && ownLocationModel.hasPreciseLocation()
                    && locationUpdateManager.isUpdating()) {
                jsonObject.put("location", ownLocationModel.getLocationJson());
            }

            if (chatModel.hasOutgoingMessages()) {
                JSONArray messages = chatModel.getOutgoingMessagesAsJson();
                jsonObject.put("messages", messages);
            }
        } catch (JSONException e) {
            Timber.e(e);
        }
        return jsonObject;
    }
}
