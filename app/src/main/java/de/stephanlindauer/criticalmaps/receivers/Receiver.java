package de.stephanlindauer.criticalmaps.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class Receiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String referrer = intent.getStringExtra("referrer");
        System.out.println("referrer test: " + referrer);
    }
}
