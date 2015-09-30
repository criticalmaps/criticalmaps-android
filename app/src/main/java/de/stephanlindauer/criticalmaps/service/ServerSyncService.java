package de.stephanlindauer.criticalmaps.service;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import java.util.Timer;
import java.util.TimerTask;

import de.stephanlindauer.criticalmaps.handler.PullServerHandler;
import de.stephanlindauer.criticalmaps.utils.TrackingInfoNotificationBuilder;

public class ServerSyncService extends Service {

    private final int PULL_OTHER_LOCATIONS_TIME = 12 * 1000; // 12 sec -> 5 times a minute

    private Timer timerPullServer;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        startForeground(TrackingInfoNotificationBuilder.NOTIFICATION_ID,
                TrackingInfoNotificationBuilder.getNotification(getApplication()));

        timerPullServer = new Timer();

        TimerTask timerTaskPullServer = new TimerTask() {
            @Override
            public void run() {
                // Since JELLYBEAN AsyncTask makes sure it's started from
                // the UI thread. Before that we have do to that ourselves.
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            new PullServerHandler().execute();
                        }
                    });
                } else {
                    new PullServerHandler().execute();
                }
            }
        };
        timerPullServer.scheduleAtFixedRate(timerTaskPullServer, 0, PULL_OTHER_LOCATIONS_TIME);
    }

    @Override
    public void onDestroy() {
        timerPullServer.cancel();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        stopSelf();
        super.onTaskRemoved(rootIntent);
    }
}
