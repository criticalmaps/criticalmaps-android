package de.stephanlindauer.criticalmaps.utils;

import android.app.Application;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import de.stephanlindauer.criticalmaps.Main;
import de.stephanlindauer.criticalmaps.R;

public class TrackingInfoNotificationBuilder {

    //const
    public static final int NOTIFICATION_ID = 12456;
    private static final int INTENT_CLOSE_ID = 176456;
    private static final int INTENT_OPEN_ID = 133256;

    public static Notification getNotification(Application application) {
        Intent openIntent = new Intent(application, Main.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        openIntent.putExtra("shouldClose", false);
        PendingIntent openPendingIntent = PendingIntent.getActivity(application, INTENT_OPEN_ID, openIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent closeIntent = new Intent(application, Main.class);
        closeIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        closeIntent.putExtra("shouldClose", true);
        PendingIntent closePendingIntent = PendingIntent.getActivity(application, INTENT_CLOSE_ID, closeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(application)
                .setSmallIcon(R.drawable.ic_logo)
                .setContentTitle(application.getString(R.string.notification_tracking_title))
                .setContentText(application.getString(R.string.notification_tracking_text))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(application.getString(R.string.notification_tracking_text)))
                .setPriority(Notification.PRIORITY_MAX)
                .setContentIntent(openPendingIntent)
                .addAction(R.drawable.ic_action_location_found, application.getString(R.string.notification_tracking_open), openPendingIntent)
                .addAction(R.drawable.ic_action_cancel, application.getString(R.string.notification_tracking_close), closePendingIntent);

        return builder.build();
    }
}
