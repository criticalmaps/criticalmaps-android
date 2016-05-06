package de.stephanlindauer.criticalmaps.vo;

import android.content.SharedPreferences;

import de.stephanlindauer.criticalmaps.prefs.SharedPrefsKeys;
import info.metadude.android.typedpreferences.BooleanPreference;

public class RouteConfiguration {
    private RoutesCategory routesCategory;
    private final int resourceId;
    private final String name;
    private final String sharedPrefsKey;

    public RouteConfiguration(String name, int resourceId, RoutesCategory routesCategory) {
        this.name = name;
        this.resourceId = resourceId;
        this.routesCategory = routesCategory;
        sharedPrefsKey = SharedPrefsKeys.ROUTE_PREFIX + " - " + routesCategory.getName() + " - " + name;
    }

    public boolean isActive(SharedPreferences sharedPreferences) {
        if (!routesCategory.isActive(sharedPreferences)) {
            return false;
        }
        return sharedPreferences.getBoolean(sharedPrefsKey, false);
    }

    public String getName() {
        return name;
    }

    public void setActive(boolean active, SharedPreferences sharedPreferences) {
        new BooleanPreference(sharedPreferences, sharedPrefsKey).set(active);
    }
}
