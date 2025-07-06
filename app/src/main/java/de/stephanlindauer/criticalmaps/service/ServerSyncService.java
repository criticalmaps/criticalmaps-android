package de.stephanlindauer.criticalmaps.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.core.content.ContextCompat;

import com.squareup.otto.Subscribe;

import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;
import javax.inject.Provider;

import de.stephanlindauer.criticalmaps.App;
import de.stephanlindauer.criticalmaps.events.NetworkConnectivityChangedEvent;
import de.stephanlindauer.criticalmaps.handler.NetworkConnectivityChangeHandler;
import de.stephanlindauer.criticalmaps.handler.PutLocationHandler;
import de.stephanlindauer.criticalmaps.managers.LocationUpdateManager;
import de.stephanlindauer.criticalmaps.provider.EventBus;
import de.stephanlindauer.criticalmaps.utils.TrackingInfoNotificationBuilder;

public class ServerSyncService extends Service {

    @SuppressWarnings("FieldCanBeLocal")
    private final int SERVER_SYNC_INTERVAL = 30 * 1000; // 30 sec

    private Timer locationUploadTimer;

    @Inject
    LocationUpdateManager locationUpdateManager;

    @Inject
    NetworkConnectivityChangeHandler networkConnectivityChangeHandler;

    @Inject
    Provider<PutLocationHandler> putLocationHandler;

    @Inject
    EventBus eventBus;

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

        eventBus.register(this);
    }

    private void startLocationUploadTimer() {
        locationUploadTimer = new Timer();

        TimerTask timerTaskPullServer = new TimerTask() {
            @Override
            public void run() {
                putLocationHandler.get().execute();
            }
        };
        locationUploadTimer.schedule(timerTaskPullServer, 0, SERVER_SYNC_INTERVAL);
    }

    private void stopLocationUploadTimer() {
        if (locationUploadTimer != null) {
            locationUploadTimer.cancel();
            locationUploadTimer = null;
        }
    }

    @Override
    public void onDestroy() {
        eventBus.unregister(this);
        locationUpdateManager.handleShutdown();
        networkConnectivityChangeHandler.stop();
        stopLocationUploadTimer();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        stopSelf();
        super.onTaskRemoved(rootIntent);
    }

    @Subscribe
    public void handleNetworkConnectivityChanged(NetworkConnectivityChangedEvent e) {
        if (e.isConnected && locationUploadTimer == null) {
            startLocationUploadTimer();
        } else {
            stopLocationUploadTimer();
        }
    }

    public static void startService() {
        App app = App.components().app();
        Intent syncServiceIntent = new Intent(app, ServerSyncService.class);
        ContextCompat.startForegroundService(app, syncServiceIntent);
    }

    public static void stopService() {
        App app = App.components().app();
        Intent syncServiceIntent = new Intent(app, ServerSyncService.class);
        app.stopService(syncServiceIntent);
    }
}
