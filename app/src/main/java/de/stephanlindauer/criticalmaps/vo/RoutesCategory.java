package de.stephanlindauer.criticalmaps.vo;


import android.content.SharedPreferences;

import java.util.ArrayList;

import de.stephanlindauer.criticalmaps.prefs.SharedPrefsKeys;
import info.metadude.android.typedpreferences.BooleanPreference;

public class RoutesCategory {
    private final String name;
    private final String sharedPrefsKey;
    private ArrayList<RouteConfiguration> routeConfigurations = new ArrayList<RouteConfiguration>();

    public RoutesCategory(String name) {
        this.name = name;
        sharedPrefsKey = SharedPrefsKeys.ROUTES_CATEGORY_PREFIX + " - " + name;
    }

    public void add(RouteConfiguration routeConfiguration) {
        routeConfigurations.add(routeConfiguration);
    }

    public ArrayList<RouteConfiguration> getRouteConfigurations() {
        return routeConfigurations;
    }

    public String getName() {
        return name;
    }

    public boolean isActive(SharedPreferences sharedPreferences) {
        return sharedPreferences.getBoolean(sharedPrefsKey, false);
    }

    public void setActive(boolean active, SharedPreferences sharedPreferences) {
        new BooleanPreference(sharedPreferences, sharedPrefsKey).set(active);
    }
}
