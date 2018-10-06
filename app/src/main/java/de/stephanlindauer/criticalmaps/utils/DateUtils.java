package de.stephanlindauer.criticalmaps.utils;

import android.support.annotation.NonNull;

import java.util.Date;

public class DateUtils {

    private DateUtils() {}

    public static boolean isNotLongerAgoThen(@NonNull Date date, int minutes, int seconds) {
        long differenceInMs = new Date().getTime() - date.getTime();
        return differenceInMs <= (minutes * 60 + seconds) * 1000L;
    }
}
