package de.stephanlindauer.criticalmaps.service;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import com.squareup.otto.Subscribe;

import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;
import javax.inject.Provider;

import de.stephanlindauer.criticalmaps.App;
import de.stephanlindauer.criticalmaps.events.NetworkConnectivityChangedEvent;
import de.stephanlindauer.criticalmaps.handler.NetworkConnectivityChangeHandler;
import de.stephanlindauer.criticalmaps.handler.PullServerHandler;
import de.stephanlindauer.criticalmaps.managers.LocationUpdateManager;
import de.stephanlindauer.criticalmaps.provider.EventBusProvider;
import de.stephanlindauer.criticalmaps.utils.TrackingInfoNotificationBuilder;

public class ServerSyncService extends Service {

    @SuppressWarnings("FieldCanBeLocal")
    private final int SERVER_SYNC_INTERVAL = 12 * 1000; // 12 sec -> 5 times a minute

    private Timer timerPullServer;

    @Inject
    LocationUpdateManager locationUpdateManager;

    @Inject
    NetworkConnectivityChangeHandler networkConnectivityChangeHandler;

    @Inject
    Provider<PullServerHandler> pullServerHandler;

    @Inject
    EventBusProvider eventBusProvider;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        App.components().inject(this);

        startForeground(TrackingInfoNotificationBuilder.NOTIFICATION_ID,
                TrackingInfoNotificationBuilder.getNotification(getApplication()));

        locationUpdateManager.initializeAndStartListening();

        networkConnectivityChangeHandler.start();

        eventBusProvider.register(this);
    }

    private void startPullServerTimer() {
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
                            pullServerHandler.get().execute();
                        }
                    });
                } else {
                    pullServerHandler.get().execute();
                }
            }
        };
        timerPullServer.scheduleAtFixedRate(timerTaskPullServer, 0, SERVER_SYNC_INTERVAL);
    }

    private void stopPullServerTimer() {
        if (timerPullServer != null) {
            timerPullServer.cancel();
            timerPullServer = null;
        }
    }

    @Override
    public void onDestroy() {
        eventBusProvider.unregister(this);
        locationUpdateManager.handleShutdown();
        networkConnectivityChangeHandler.stop();
        stopPullServerTimer();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        stopSelf();
        super.onTaskRemoved(rootIntent);
    }

    @Subscribe
    public void handleNetworkConnectivityChanged(NetworkConnectivityChangedEvent e) {
        if (e.isConnected && timerPullServer == null) {
            startPullServerTimer();
        } else {
            stopPullServerTimer();
        }
    }

    public static void startService() {
        App app = App.components().app();
        Intent syncServiceIntent = new Intent(app, ServerSyncService.class);
        app.startService(syncServiceIntent);
    }

    public static void stopService() {
        App app = App.components().app();
        Intent syncServiceIntent = new Intent(app, ServerSyncService.class);
        app.stopService(syncServiceIntent);
    }
}
