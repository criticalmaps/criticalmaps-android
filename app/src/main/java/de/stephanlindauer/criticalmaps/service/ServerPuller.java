package de.stephanlindauer.criticalmaps.service;

import android.app.Activity;

import java.util.Timer;
import java.util.TimerTask;

import de.stephanlindauer.criticalmaps.handler.PullServerHandler;

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
