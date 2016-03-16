package de.stephanlindauer.criticalmaps.utils;

import android.support.annotation.NonNull;

import java.util.Date;

public class DateUtils {

    private DateUtils() {}

    public static boolean isNotLongerAgoThen(@NonNull Date timestamp, int minutes, int seconds) {
        long differenceInMs = timestamp.getTime() - new Date().getTime();
        return differenceInMs <= (minutes * 60 + seconds) * 1000;
    }
}
