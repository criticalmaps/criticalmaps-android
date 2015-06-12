package de.stephanlindauer.criticalmaps.service;

import android.app.Activity;
import android.os.AsyncTask;
import android.provider.Settings;
import com.crashlytics.android.Crashlytics;
import de.stephanlindauer.criticalmaps.events.NewServerResponseEvent;
import de.stephanlindauer.criticalmaps.handler.PullServerHandler;
import de.stephanlindauer.criticalmaps.helper.AeSimpleSHA1;
import de.stephanlindauer.criticalmaps.model.ChatModel;
import de.stephanlindauer.criticalmaps.model.OtherUsersLocationModel;
import de.stephanlindauer.criticalmaps.model.OwnLocationModel;
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
import java.util.Timer;
import java.util.TimerTask;

public class ServerPuller {


    private final int PULL_OTHER_LOCATIONS_TIME = 20 * 1000; //20 sec

    //misc
    private Activity activity;
    private Timer timerPullServer;
    private TimerTask timerTaskPullServer;

    //singleton
    private static ServerPuller instance;

    public static ServerPuller getInstance() {
        if (ServerPuller.instance == null) {
            ServerPuller.instance = new ServerPuller();
        }
        return ServerPuller.instance;
    }

    public void initialize(final Activity activity) {
        timerPullServer = new Timer();

        timerTaskPullServer = new TimerTask() {
            @Override
            public void run() {
                activity.runOnUiThread(
                        new Runnable() {
                            @Override
                            public void run() { //da fuq!!?
                                pullServer();
                            }
                        }
                );
            }
        };
        timerPullServer.scheduleAtFixedRate(timerTaskPullServer, 0, PULL_OTHER_LOCATIONS_TIME);
    }

    private void pullServer() {
        new PullServerHandler().execute();
    }
}
