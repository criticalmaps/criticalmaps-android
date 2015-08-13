package de.stephanlindauer.criticalmaps.utils;

import java.util.Date;

public class DateUtils {
    public static boolean isLongerAgoThen5Minutes(Date timestamp) {
        long differenceInMs = timestamp.getTime() - new Date().getTime();
        return differenceInMs >= 5 * 60 * 1000;
    }
}
