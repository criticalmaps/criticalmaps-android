package de.stephanlindauer.criticalmaps.model;

import android.app.Activity;
import android.provider.Settings;
import de.stephanlindauer.criticalmaps.utils.AeSimpleSHA1;

import static android.provider.Settings.Secure.getString;

public class UserModel {

    private String uniqueDeviceIdHashed;

    //singleton
    private static UserModel instance;

    private UserModel() {}

    public static UserModel getInstance() {
        if (UserModel.instance == null) {
            UserModel.instance = new UserModel();
        }
        return UserModel.instance;
    }

    public void initialize(Activity activity) {
        final String androidId = getString(activity.getContentResolver(), Settings.Secure.ANDROID_ID);
        uniqueDeviceIdHashed = AeSimpleSHA1.SHA1(androidId);
    }

    public String getUniqueDeviceIdHashed() {
        return uniqueDeviceIdHashed;
    }
}
