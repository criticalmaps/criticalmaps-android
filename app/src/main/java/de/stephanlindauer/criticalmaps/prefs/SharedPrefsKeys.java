package de.stephanlindauer.criticalmaps.prefs;

import de.stephanlindauer.criticalmaps.BuildConfig;

public interface SharedPrefsKeys {
    String INTRODUCTION_ALREADY_SHOWN =
            BuildConfig.APPLICATION_ID + ".INTRODUCTION_ALREADY_SHOWN";
    String OSMDROID_BASE_PATH =
            BuildConfig.APPLICATION_ID + ".OSMDROID_BASE_PATH";
}
