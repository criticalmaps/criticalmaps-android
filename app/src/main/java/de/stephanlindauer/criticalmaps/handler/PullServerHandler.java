package de.stephanlindauer.criticalmaps.handler;

import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import de.stephanlindauer.criticalmaps.events.NewServerResponseEvent;
import de.stephanlindauer.criticalmaps.model.ChatModel;
import de.stephanlindauer.criticalmaps.model.OtherUsersLocationModel;
import de.stephanlindauer.criticalmaps.model.OwnLocationModel;
import de.stephanlindauer.criticalmaps.model.UserModel;
import de.stephanlindauer.criticalmaps.service.EventService;
import de.stephanlindauer.criticalmaps.vo.Endpoints;

public class PullServerHandler extends AsyncTask<Void, Void, String> {

    //const
    public static final int TIME_OUT = 15 * 1000; //15 sec

    //dependencies
    private final OtherUsersLocationModel otherUsersLocationModel = OtherUsersLocationModel.getInstance();
    private final ChatModel chatModel = ChatModel.getInstance();
    private final OwnLocationModel ownLocationModel = OwnLocationModel.getInstance();
    private final EventService eventService = EventService.getInstance();
    private final UserModel userModel = UserModel.getInstance();

    @Override
    protected String doInBackground(Void... params) {
        String jsonPostString = getJsonObject().toString();

        final HttpPost postRequest = new HttpPost(Endpoints.MAIN_POST);

        final HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, TIME_OUT);

        try {
            postRequest.setEntity(new StringEntity(jsonPostString));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        final HttpClient httpClient = new DefaultHttpClient(httpParams);

        String responseString = "";
        try {
            HttpResponse response = httpClient.execute(postRequest);
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                out.close();
                responseString = out.toString();
            } else {
                response.getEntity().getContent().close();
            }
        } catch (IOException e) {
//            Crashlytics.logException(e);
        }
        return responseString;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(result);
            otherUsersLocationModel.setNewJSON(jsonObject.getJSONObject("locations"));
            chatModel.setNewJson(jsonObject.getJSONObject("chatMessages"));
        } catch (Exception e) {

        } finally {
            eventService.post(new NewServerResponseEvent());
        }
    }

    private JSONObject getJsonObject() {
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("device", userModel.getUniqueDeviceIdHashed());

            if (ownLocationModel.ownLocation != null) {
                JSONObject locationObject = new JSONObject();
                locationObject.put("longitude", Integer.toString(ownLocationModel.ownLocation.getLongitudeE6()));
                locationObject.put("latitude", Integer.toString(ownLocationModel.ownLocation.getLatitudeE6()));
                jsonObject.put("location", locationObject);
            }

            if (chatModel.hasOutgoingMessages()) {
                JSONArray messages = chatModel.getOutgoingMessagesAsJson();
                jsonObject.put("messages", messages);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }
}
