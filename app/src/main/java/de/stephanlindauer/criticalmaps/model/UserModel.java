package de.stephanlindauer.criticalmaps.model;

import android.app.Activity;
import android.provider.Settings;
import de.stephanlindauer.criticalmaps.utils.AeSimpleSHA1;

import static android.provider.Settings.Secure.getString;

public class UserModel {

    private String uniqueDeviceIdHashed;

    public void initialize(Activity activity) {
        final String androidId = getString(activity.getContentResolver(), Settings.Secure.ANDROID_ID);
        uniqueDeviceIdHashed = AeSimpleSHA1.SHA1(androidId);
    }

    public String getUniqueDeviceIdHashed() {
        return uniqueDeviceIdHashed;
    }
}
