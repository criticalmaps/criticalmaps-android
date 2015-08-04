package de.stephanlindauer.criticalmaps.helper.clientinfo;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class BuildInfo {
    public static String getString(PackageManager packageManager, String packageName) {
        String returnString = "";

        try {
            PackageInfo pInfo = packageManager.getPackageInfo(packageName, 0);
            returnString += "\n---------------------------------------------------------------\n";
            returnString += "VERSION_NAME= " + pInfo.versionName + "\n";
            returnString += "VERSION_CODE= " + pInfo.versionCode + "\n";
        } catch (PackageManager.NameNotFoundException e) {
        }
        return returnString;
    }
}