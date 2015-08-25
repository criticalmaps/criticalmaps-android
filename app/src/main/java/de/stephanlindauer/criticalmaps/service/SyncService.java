package de.stephanlindauer.criticalmaps.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

import de.stephanlindauer.criticalmaps.notifications.trackinginfo.TrackingInfoNotificationSetter;

public class SyncService extends Service {
    private Timer timerPullServer;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        timerPullServer = new Timer();

        TimerTask timerTaskPullServer = new TimerTask() {
            @Override
            public void run() {
                Log.d("foo-service ", "miau miau");
            }
        };
        timerPullServer.scheduleAtFixedRate(timerTaskPullServer, 0, 10000);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        TrackingInfoNotificationSetter.getInstance().cancel();
        stopSelf();
        super.onTaskRemoved(rootIntent);
    }
}
