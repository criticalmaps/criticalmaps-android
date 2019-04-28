package de.stephanlindauer.criticalmaps.helper.clientinfo;

import android.os.Build;
import android.text.TextUtils;

public class DeviceInformation {
    public static String getString() {
        String returnString = "";
        returnString += "\n\n---------------------------------------------------------------\n";
        returnString += "OS_VERSION= " + System.getProperty("os.version") + "\n";
        returnString += "RELEASE= " + Build.VERSION.RELEASE + "\n";
        returnString += "DEVICE= " + Build.DEVICE + "\n";
        returnString += "MODEL= " + Build.MODEL + "\n";
        returnString += "PRODUCT= " + Build.PRODUCT + "\n";
        returnString += "BRAND= " + Build.BRAND + "\n";
        returnString += "DISPLAY= " + Build.DISPLAY + "\n";
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            returnString += "CPU_ABI= " + Build.CPU_ABI + "\n";
            returnString += "CPU_ABI2= " + Build.CPU_ABI2 + "\n";
        } else {
            returnString += "SUPPORTED_ABIS= " + TextUtils.join(", ", Build.SUPPORTED_ABIS) + "\n";
        }
        returnString += "HARDWARE= " + Build.HARDWARE + "\n";
        returnString += "ID= " + Build.ID + "\n";
        returnString += "MANUFACTURER= " + Build.MANUFACTURER + "\n";
        returnString += "USER= " + Build.USER + "\n";
        returnString += "HOST= " + Build.HOST + "\n";

        return returnString;
    }
}
