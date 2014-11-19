package de.stephanlindauer.criticalmaps.helper.clientinfo;


public class DeviceInformation {
    public static String getString() {
        String returnString = "";
        returnString += "\n\n---------------------------------------------------------------\n";
        returnString += "OS_VERSION= " + System.getProperty("os.version") + "\n";
        returnString += "RELEASE= " + android.os.Build.VERSION.RELEASE + "\n";
        returnString += "DEVICE= " + android.os.Build.DEVICE + "\n";
        returnString += "MODEL= " + android.os.Build.MODEL + "\n";
        returnString += "PRODUCT= " + android.os.Build.PRODUCT + "\n";
        returnString += "BRAND= " + android.os.Build.BRAND + "\n";
        returnString += "DISPLAY= " + android.os.Build.DISPLAY + "\n";
        returnString += "CPU_ABI= " + android.os.Build.CPU_ABI + "\n";
        returnString += "CPU_ABI2= " + android.os.Build.CPU_ABI2 + "\n";
        returnString += "UNKNOWN= " + android.os.Build.UNKNOWN + "\n";
        returnString += "HARDWARE= " + android.os.Build.HARDWARE + "\n";
        returnString += "ID= " + android.os.Build.ID + "\n";
        returnString += "MANUFACTURER= " + android.os.Build.MANUFACTURER + "\n";
        returnString += "SERIAL= " + android.os.Build.SERIAL + "\n";
        returnString += "USER= " + android.os.Build.USER + "\n";
        returnString += "HOST= " + android.os.Build.HOST + "\n";

        return returnString;
    }
}
