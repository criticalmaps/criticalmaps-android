package de.stephanlindauer.criticalmass.notifications.reminder;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;
import java.util.Date;

public class ReminderNotificationSetter {

    private final Context context;
    private final Activity activity;

    public ReminderNotificationSetter(Context context, Activity activity) {
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
                = NextEventTimeCalculator.getNextThreeCriticalMassDates();

        for (Date cmDate : nextThreeDates) {
            alarmManager.set( AlarmManager.RTC_WAKEUP , cmDate.getTime(), pendingIntent );
        }
    }
}
