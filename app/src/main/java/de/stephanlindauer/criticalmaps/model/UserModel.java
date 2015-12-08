package de.stephanlindauer.criticalmaps.model;

import android.app.Activity;
import android.provider.Settings;

import java.text.SimpleDateFormat;
import java.util.Date;

import de.stephanlindauer.criticalmaps.utils.AeSimpleSHA1;

import static android.provider.Settings.Secure.getString;

public class UserModel {

    private String uniqueDeviceId;

    public void initialize(Activity activity) {
        final String androidId = getString(activity.getContentResolver(), Settings.Secure.ANDROID_ID);
        final String dateString = new SimpleDateFormat("yyyy-mm-dd").format(new Date());
        uniqueDeviceId = AeSimpleSHA1.SHA1(androidId + dateString);
    }

    public String getUniqueDeviceId() {
        return uniqueDeviceId;
    }
}
