package de.stephanlindauer.criticalmaps.helper.clientinfo;

import de.stephanlindauer.criticalmaps.BuildConfig;

public class BuildInfo {
    public static String getString() {
        return "\n---------------------------------------------------------------\n"
                + "VERSION_NAME= " + BuildConfig.VERSION_NAME + "\n"
                + "VERSION_CODE= " + BuildConfig.VERSION_CODE + "\n";
    }
}