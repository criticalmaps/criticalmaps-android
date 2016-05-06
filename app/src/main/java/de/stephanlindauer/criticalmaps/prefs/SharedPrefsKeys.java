package de.stephanlindauer.criticalmaps.prefs;

import de.stephanlindauer.criticalmaps.BuildConfig;

public interface SharedPrefsKeys {

    String INTRODUCTION_ALREADY_SHOWN =
            BuildConfig.APPLICATION_ID + ".INTRODUCTION_ALREADY_SHOWN";

    String LAST_KNOWN_LOCATION =
            BuildConfig.APPLICATION_ID + ".LAST_KNOWN_LOCATION";

    String TIME_STAMP =
            BuildConfig.APPLICATION_ID + ".TIME_STAMP";

    String ROUTE_PREFIX =
            BuildConfig.APPLICATION_ID + ".ROUTE_PREFIX";

    String ROUTES_CATEGORY_PREFIX =
            BuildConfig.APPLICATION_ID + ".ROUTES_CATEGORY_PREFIX";

}
