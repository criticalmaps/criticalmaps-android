package de.stephanlindauer.criticalmaps.service;

import android.app.Activity;
import android.os.AsyncTask;
import android.provider.Settings;
import com.crashlytics.android.Crashlytics;
import de.stephanlindauer.criticalmaps.events.NewServerResponseEvent;
import de.stephanlindauer.criticalmaps.helper.AeSimpleSHA1;
import de.stephanlindauer.criticalmaps.model.ChatModel;
import de.stephanlindauer.criticalmaps.model.OtherUsersLocationModel;
import de.stephanlindauer.criticalmaps.model.OwnLocationModel;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class ServerPuller {

    //const
    public static final int TIME_OUT = 15 * 1000; //15 sec
    private final int PULL_OTHER_LOCATIONS_TIME = 20 * 1000; //20 sec

    //dependencies
    private final OtherUsersLocationModel otherUsersLocationModel = OtherUsersLocationModel.getInstance();
    private final ChatModel chatModel = ChatModel.getInstance();
    private final OwnLocationModel ownLocationModel = OwnLocationModel.getInstance();
    private final EventService eventService = EventService.getInstance();

    //misc
    private Activity activity;
    private Timer timerPullServer;
    private TimerTask timerTaskPullServer;

    private String uniqueDeviceIdHashed;
    private String message;

    //singleton
    private static ServerPuller instance;

    public static ServerPuller getInstance() {
        if (ServerPuller.instance == null) {
            ServerPuller.instance = new ServerPuller();
        }
        return ServerPuller.instance;
    }

    public void initialize(final Activity activity) {
        this.activity = activity;
        this.uniqueDeviceIdHashed = AeSimpleSHA1.SHA1(Settings.Secure.getString(activity.getContentResolver(),
                Settings.Secure.ANDROID_ID));

        timerPullServer = new Timer();

        timerTaskPullServer = new TimerTask() {
            @Override
            public void run() {
                activity.runOnUiThread(
                        new Runnable() {
                            @Override
                            public void run() {
                                pullServer();
                            }
                        }
                );
            }
        };
        timerPullServer.scheduleAtFixedRate(timerTaskPullServer, 0, PULL_OTHER_LOCATIONS_TIME);
    }

    public void addOutGoingMessageAndTriggerRequest(String message) {
        this.message = message;
        activity.runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        pullServer();
                    }
                }
        );
    }

    private void pullServer() {
        final HttpPost postRequest = new HttpPost("http://api.criticalmaps.net/post");
        ArrayList<NameValuePair> postParams = new ArrayList<NameValuePair>(2);

        postParams.add(new BasicNameValuePair("device", uniqueDeviceIdHashed));

        if (ownLocationModel.ownLocation != null) {
            postParams.add(new BasicNameValuePair("longitude", Integer.toString(ownLocationModel.ownLocation.getLongitudeE6())));
            postParams.add(new BasicNameValuePair("latitude", Integer.toString(ownLocationModel.ownLocation.getLatitudeE6())));
        }

        if (chatModel.hasOutgoingMessages()) {
            String urlEncodedMessages = chatModel.getOutgoingMessagesAsJson(uniqueDeviceIdHashed).toString();
            postParams.add(new BasicNameValuePair("messages", urlEncodedMessages));
        }

        final HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, TIME_OUT);

        try {
            postRequest.setEntity(new UrlEncodedFormEntity(postParams));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        final HttpClient httpClient = new DefaultHttpClient(httpParams);

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
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
                    Crashlytics.logException(e);
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
                } catch (JSONException e) {
                    Crashlytics.logException(e);
                } catch (UnsupportedEncodingException e) {
                    Crashlytics.logException(e);
                }
                eventService.post(new NewServerResponseEvent());
            }
        }.execute();
    }
}
