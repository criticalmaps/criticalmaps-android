package de.stephanlindauer.criticalmaps.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class Receiver extends BroadcastReceiver {
    //TODO remove this whole class when deeplinking works properly
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.hasExtra("referrer")) {
            String referrer = intent.getStringExtra("referrer");
            System.out.println("referrer test 'referrer': " + referrer);
        }
        //this serves as a fallback when chrome overrides "referrer"
        if (intent.hasExtra("foobar")) {
            String fallbackReferrer = intent.getStringExtra("foobar");
            System.out.println("referrer test: 'foobar'" + fallbackReferrer);
        }
    }
}
