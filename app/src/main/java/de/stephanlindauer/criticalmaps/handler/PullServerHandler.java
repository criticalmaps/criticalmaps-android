package de.stephanlindauer.criticalmaps.handler;

import android.os.AsyncTask;
import android.util.Log;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import javax.inject.Inject;

import de.stephanlindauer.criticalmaps.model.ChatModel;
import de.stephanlindauer.criticalmaps.model.OwnLocationModel;
import de.stephanlindauer.criticalmaps.model.UserModel;
import de.stephanlindauer.criticalmaps.vo.Endpoints;

public class PullServerHandler extends AsyncTask<Void, Void, String> {

    //const
    private static final String LOG_TAG = "CM_PullServerHandler";

    //dependencies
    private final ChatModel chatModel;
    private final OwnLocationModel ownLocationModel;
    private final UserModel userModel;
    private final ServerResponseProcessor serverResponseProcessor;
    private final OkHttpClient okHttpClient;

    @Inject
    public PullServerHandler(ChatModel chatModel, OwnLocationModel ownLocationModel, UserModel userModel, ServerResponseProcessor serverResponseProcessor, OkHttpClient okHttpClient) {
        this.chatModel = chatModel;
        this.ownLocationModel = ownLocationModel;
        this.userModel = userModel;
        this.serverResponseProcessor = serverResponseProcessor;
        this.okHttpClient = okHttpClient;
    }

    @Override
    protected String doInBackground(Void... params) {
        String jsonPostString = getJsonObject().toString();

        final RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonPostString);
        final Request postRequest = new Request.Builder().url(Endpoints.MAIN_POST).post(body).build();

        try {
            final Response response = okHttpClient.newCall(postRequest).execute();
            if (response.isSuccessful()) {
                String responseBodyString = response.body().string();
                response.body().close();
                return responseBodyString;
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, Log.getStackTraceString(e));
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

            if (ownLocationModel.hasPreciseLocation() && ownLocationModel.isLocationFresh()) {
                jsonObject.put("location", ownLocationModel.getLocationJson());
            }

            if (chatModel.hasOutgoingMessages()) {
                JSONArray messages = chatModel.getOutgoingMessagesAsJson();
                jsonObject.put("messages", messages);
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, Log.getStackTraceString(e));
        }
        return jsonObject;
    }
}
