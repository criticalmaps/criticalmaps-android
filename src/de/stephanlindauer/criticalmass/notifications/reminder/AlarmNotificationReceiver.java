package de.stephanlindauer.criticalmass.notifications.reminder;

import android.annotation.TargetApi;

import android.app.*;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import de.stephanlindauer.criticalmass.Main;
import de.stephanlindauer.criticalmass.R;

public class AlarmNotificationReceiver extends BroadcastReceiver {

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onReceive(Context context, Intent intent) {

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(context.getResources().getString(R.string.notification_alarm_title))
                        .setContentText(context.getResources().getString(R.string.notification_alarm_text))
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(context.getResources().getString(R.string.notification_alarm_text)));
        Intent resultIntent = new Intent(context, Main.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(Main.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Notification notification = mBuilder.build();

        notification.sound = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.notification);
        notification.flags |=  Notification.FLAG_AUTO_CANCEL |  Notification.FLAG_ONLY_ALERT_ONCE;
        notification.defaults |= Notification.DEFAULT_LIGHTS;

        mNotificationManager.notify(1, notification);
    }
}