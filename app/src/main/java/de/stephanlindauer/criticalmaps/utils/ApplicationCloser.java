package de.stephanlindauer.criticalmaps.utils;

import android.app.Activity;
import android.content.Intent;

import de.stephanlindauer.criticalmaps.App;
import de.stephanlindauer.criticalmaps.service.ServerSyncService;

public class ApplicationCloser {
    public static void close(Activity activity) {
        App.components().locationUpdatesService().handleShutdown();
        Intent syncServiceIntent = new Intent(activity, ServerSyncService.class);
        activity.stopService(syncServiceIntent);
        activity.finish();
    }
}
