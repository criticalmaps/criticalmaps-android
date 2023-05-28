package de.stephanlindauer.criticalmaps.handler;

import android.content.SharedPreferences;
import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import javax.inject.Inject;

import de.stephanlindauer.criticalmaps.managers.LocationUpdateManager;
import de.stephanlindauer.criticalmaps.model.ChatModel;
import de.stephanlindauer.criticalmaps.model.OwnLocationModel;
import de.stephanlindauer.criticalmaps.model.UserModel;
import de.stephanlindauer.criticalmaps.prefs.SharedPrefsKeys;
import de.stephanlindauer.criticalmaps.vo.Endpoints;
import info.metadude.android.typedpreferences.BooleanPreference;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import timber.log.Timber;

public class HeartbeatHandler extends AsyncTask<Void, Void, Void> {

    //dependencies
    private final ChatModel chatModel;
    private final OwnLocationModel ownLocationModel;
    private final UserModel userModel;
    private final ServerResponseProcessor serverResponseProcessor;
    private final OkHttpClient okHttpClient;
    private final SharedPreferences sharedPreferences;
    private final LocationUpdateManager locationUpdateManager;

    @Inject
    public HeartbeatHandler(ChatModel chatModel,
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
    protected Void doInBackground(Void... params) {
        final boolean isObserverModeActive = new BooleanPreference(
                sharedPreferences, SharedPrefsKeys.OBSERVER_MODE_ACTIVE).get();

        if (!isObserverModeActive && ownLocationModel.hasPreciseLocation()
                && locationUpdateManager.isUpdating()) {
            Timber.d("Heartbeat preconditions are fulfilled.");
        } else {
            Timber.d("Heartbeat preconditions are not fulfilled.");
            return null;
        }

        String jsonPostString = getJsonObject().toString();

        final RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonPostString);
        final Request request = new Request.Builder().url(Endpoints.LOCATION_PUT).put(body).build();

        try {
            final Response response = okHttpClient.newCall(request).execute();
            if (!response.isSuccessful()) {
                //TODO Display error to user
            }
        } catch (IOException e) {
            Timber.e(e);
        }
        return null;
    }


    private JSONObject getJsonObject() {
        JSONObject jsonObject;
        jsonObject = ownLocationModel.getLocationJson();

        try{
            jsonObject.put("device", userModel.getChangingDeviceToken());
        } catch (JSONException e) {
            Timber.e(e);
        }
        
        return jsonObject;
    }
}
