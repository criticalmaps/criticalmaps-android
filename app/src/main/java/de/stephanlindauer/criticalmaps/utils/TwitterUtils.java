package de.stephanlindauer.criticalmaps.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TwitterUtils {
    public static Date getTwitterDate(String date) throws ParseException {
        String twitterTimeFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(twitterTimeFormat);
        sf.setLenient(true);
        return sf.parse(date);
    }
}
