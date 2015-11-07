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
import java.net.HttpURLConnection;

import de.stephanlindauer.criticalmaps.model.ChatModel;
import de.stephanlindauer.criticalmaps.model.OtherUsersLocationModel;
import de.stephanlindauer.criticalmaps.model.OwnLocationModel;
import de.stephanlindauer.criticalmaps.model.UserModel;
import de.stephanlindauer.criticalmaps.provider.EventBusProvider;
import de.stephanlindauer.criticalmaps.provider.HttpClientProvider;
import de.stephanlindauer.criticalmaps.vo.Endpoints;

public class PullServerHandler extends AsyncTask<Void, Void, String> {

    //const
    private static final String LOG_TAG = "CM_PullServerHandler";

    //dependencies
    private final OtherUsersLocationModel otherUsersLocationModel = OtherUsersLocationModel.getInstance();
    private final ChatModel chatModel = ChatModel.getInstance();
    private final OwnLocationModel ownLocationModel = OwnLocationModel.getInstance();
    private final EventBusProvider eventService = EventBusProvider.getInstance();
    private final UserModel userModel = UserModel.getInstance();

    private final ServerResponseProcessor serverResponseProcessor = new ServerResponseProcessor(otherUsersLocationModel, eventService, chatModel);

    @Override
    protected String doInBackground(Void... params) {
        String jsonPostString = getJsonObject().toString();

        final RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonPostString);
        final Request postRequest = new Request.Builder().url(Endpoints.MAIN_POST).post(body).build();

        final OkHttpClient okHttpClient = HttpClientProvider.get();

        try {
            final Response response = okHttpClient.newCall(postRequest).execute();
            if (response.code() == HttpURLConnection.HTTP_OK) {
                String responseBody = response.body().string();
                response.body().close();
                return responseBody;
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, Log.getStackTraceString(e));
        }
        return "";
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        serverResponseProcessor.process(result);
    }

    private JSONObject getJsonObject() {
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("device", userModel.getUniqueDeviceIdHashed());

            if (ownLocationModel.hasPreciseLocation()) {
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
