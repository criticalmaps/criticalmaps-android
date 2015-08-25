package de.stephanlindauer.criticalmaps.notifications.trackinginfo;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import de.stephanlindauer.criticalmaps.Main;
import de.stephanlindauer.criticalmaps.R;

public class TrackingInfoNotificationSetter {

    //const
    private final int NOTIFICATION_ID = 123456;
    private final int INTENT_CLOSE_ID = 176456;
    private final int INTENT_OPEN_ID = 133256;

    private Context context;
    private Activity activity;
    private NotificationManager mNotificationManager;

    //singleton
    private static TrackingInfoNotificationSetter instance;

    public static TrackingInfoNotificationSetter getInstance() {
        if (TrackingInfoNotificationSetter.instance == null) {
            TrackingInfoNotificationSetter.instance = new TrackingInfoNotificationSetter();
        }
        return TrackingInfoNotificationSetter.instance;
    }

    public void initialize(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
    }

    public void show() {
        Intent openIntent = new Intent(context, Main.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        openIntent.putExtra("shouldClose", false);
        PendingIntent openPendingIntent = PendingIntent.getActivity(activity, INTENT_OPEN_ID, openIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent closeIntent = new Intent(context, Main.class);
        closeIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        closeIntent.putExtra("shouldClose", true);
        PendingIntent closePendingIntent = PendingIntent.getActivity(activity, INTENT_CLOSE_ID, closeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_action_location_found)
                .setContentTitle(activity.getString(R.string.notification_tracking_title))
                .setContentText(activity.getString(R.string.notification_tracking_text))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(activity.getString(R.string.notification_tracking_text)))
                .setPriority(Notification.PRIORITY_MAX)
                .setContentIntent(openPendingIntent)
                .addAction(R.drawable.ic_launcher, activity.getString(R.string.notification_tracking_open), openPendingIntent)
                .addAction(R.drawable.ic_action_cancel, activity.getString(R.string.notification_tracking_close), closePendingIntent);

        Notification notification = mBuilder.build();

        mNotificationManager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIFICATION_ID, notification);
    }

    public void cancel() {
        if (mNotificationManager != null) {
            mNotificationManager.cancel(NOTIFICATION_ID);
        }
    }
}
