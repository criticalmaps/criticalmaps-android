package de.stephanlindauer.criticalmaps.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TwitterUtils {
    public static Date getTwitterDate(String date) throws ParseException {
        String twitterTimeFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(twitterTimeFormat, Locale.ENGLISH);
        sf.setLenient(true);
        return sf.parse(date);
    }
}
