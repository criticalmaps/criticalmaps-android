package de.stephanlindauer.criticalmass.helper;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class NotificationSetter {

    private final Context context;
    private final Activity activity;

    public NotificationSetter(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
    }

    public void execute()
    {
        Intent alarmIntent = new Intent(activity, AlarmNotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);

        alarmManager.cancel( pendingIntent );

        ArrayList<Date> nextThreeDates
                = NotificationTimeCalculator.getNextThreeCriticalMassDates();

        for (Date cmDate : nextThreeDates) {
            alarmManager.set( AlarmManager.RTC_WAKEUP , cmDate.getTime(), pendingIntent );
        }
    }
}
