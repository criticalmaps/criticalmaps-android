package de.stephanlindauer.criticalmaps.model;

import android.annotation.SuppressLint;
import android.provider.Settings;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Singleton;

import de.stephanlindauer.criticalmaps.App;
import de.stephanlindauer.criticalmaps.utils.AeSimpleSHA1;

import static android.provider.Settings.Secure.getString;

@Singleton
public class UserModel {

    private final String changingDeviceToken;

    @Inject
    public UserModel(App app) {

        @SuppressLint("HardwareIds")
        final String androidId = getString(app.getContentResolver(), Settings.Secure.ANDROID_ID);

        final GregorianCalendar gregorianCalendar = new GregorianCalendar();
        gregorianCalendar.add(Calendar.HOUR, 6);
        final String dateString = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(gregorianCalendar.getTime());

        changingDeviceToken = AeSimpleSHA1.SHA1(androidId + dateString);
    }

    public String getChangingDeviceToken() {
        return changingDeviceToken;
    }
}
