package de.stephanlindauer.criticalmaps.model;

import android.app.Activity;
import android.provider.Settings;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import de.stephanlindauer.criticalmaps.utils.AeSimpleSHA1;

import static android.provider.Settings.Secure.getString;

public class UserModel {

    private String changingDeviceToken;

    public void initialize(Activity activity) {
        final String androidId = getString(activity.getContentResolver(), Settings.Secure.ANDROID_ID);

        final GregorianCalendar gregorianCalendar = new GregorianCalendar();
        gregorianCalendar.add(Calendar.HOUR, 6);
        final String dateString = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(gregorianCalendar.getTime());

        changingDeviceToken = AeSimpleSHA1.SHA1(androidId + dateString);
    }

    public String getChangingDeviceToken() {
        return changingDeviceToken;
    }
}
