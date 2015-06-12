package de.stephanlindauer.criticalmaps.model;

import android.app.Activity;
import android.provider.Settings;
import de.stephanlindauer.criticalmaps.helper.AeSimpleSHA1;

public class UserModel {

    //singleton
    private static UserModel instance;

    private String uniqueDeviceIdHashed;

    public static UserModel getInstance() {
        if (UserModel.instance == null) {
            UserModel.instance = new UserModel();
        }
        return UserModel.instance;
    }

    public void initialize(Activity activity) {
        this.uniqueDeviceIdHashed = AeSimpleSHA1.SHA1(Settings.Secure.getString(activity.getContentResolver(),
                Settings.Secure.ANDROID_ID));
    }

    public String getUniqueDeviceIdHashed() {
        return uniqueDeviceIdHashed;
    }
}
