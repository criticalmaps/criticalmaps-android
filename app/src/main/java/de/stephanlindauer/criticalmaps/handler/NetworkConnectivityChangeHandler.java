package de.stephanlindauer.criticalmaps.handler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.squareup.otto.Produce;

import javax.inject.Inject;

import de.stephanlindauer.criticalmaps.App;
import de.stephanlindauer.criticalmaps.events.NetworkConnectivityChangedEvent;
import de.stephanlindauer.criticalmaps.events.Events;
import de.stephanlindauer.criticalmaps.provider.EventBusProvider;

public class NetworkConnectivityChangeHandler extends BroadcastReceiver {

    private final App app;
    private final EventBusProvider eventBus;

    private ConnectivityManager connectivityManager;
    private boolean isConnected;

    @Inject
    public NetworkConnectivityChangeHandler(App app, EventBusProvider eventBus) {
        this.app = app;
        this.eventBus = eventBus;
        connectivityManager = (ConnectivityManager) app.getSystemService(Context.CONNECTIVITY_SERVICE);
        isConnected = isConnectionAvailable();
    }

    public void start() {
        eventBus.register(this);

        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        app.registerReceiver(this, intentFilter);
    }

    public void stop() {
        app.unregisterReceiver(this);
        eventBus.unregister(this);
    }

    private boolean isConnectionAvailable() {
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnectedOrConnecting());
    }

    @Produce
    public NetworkConnectivityChangedEvent produceConnectivityChangedEvent() {
        Events.NETWORK_CONNECTIVITY_CHANGED_EVENT.isConnected = isConnected;
        return Events.NETWORK_CONNECTIVITY_CHANGED_EVENT;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO
        // It's possible to reduce overhead by checking EXTRA_NO_CONNECTIVITY;
        // requires workaround for L (and above?), see:
        // http://stackoverflow.com/questions/29677852/

        if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            // only fire event if the state changed
            if (isConnected != isConnectionAvailable()) {
                isConnected = !isConnected;
                Events.NETWORK_CONNECTIVITY_CHANGED_EVENT.isConnected = isConnected;
                eventBus.post(Events.NETWORK_CONNECTIVITY_CHANGED_EVENT);
            }
        }
    }
}
