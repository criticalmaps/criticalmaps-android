package de.stephanlindauer.criticalmass.notifications.trackinginfo;

import android.annotation.TargetApi;
import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import de.stephanlindauer.criticalmass.R;

public class TrackingInfoNotificationSetter {

    private static TrackingInfoNotificationSetter instance;

    public static final int NOTIFICATION_ID = 123456;

    private Context context;
    private Activity activity;

    private NotificationManager mNotificationManager;

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

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void show() {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(activity.getString(R.string.notification_tracking_title))
                        .setContentText(activity.getString(R.string.notification_tracking_text));

        Intent resultIntent = new Intent(context, activity.getClass());
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(resultPendingIntent);
        mNotificationManager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = mBuilder.build();

//        notification.flags |= Notification.FLAG_ONGOING_EVENT;

        mNotificationManager.notify(NOTIFICATION_ID, notification);
    }

    public void cancel() {
        if (mNotificationManager != null) {
            mNotificationManager.cancel(NOTIFICATION_ID);
        }
    }
}
