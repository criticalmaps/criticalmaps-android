package de.stephanlindauer.criticalmaps.utils;

import android.app.Activity;

import de.stephanlindauer.criticalmaps.service.ServerSyncService;

public class ApplicationCloser {
    public static void close(Activity activity) {
        ServerSyncService.stopService();
        activity.finish();
    }
}
