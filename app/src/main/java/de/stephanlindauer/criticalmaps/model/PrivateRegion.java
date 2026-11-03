package de.stephanlindauer.criticalmaps.model;

import android.content.SharedPreferences;
import android.location.Location;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.stephanlindauer.criticalmaps.prefs.SharedPrefsKeys;
import info.metadude.android.typedpreferences.BooleanPreference;
import timber.log.Timber;

public class PrivateRegion {
    private final String id;
    private final double latitude;
    private final double longitude;
    private final float radius; // in meters, 200 to 2000

    public PrivateRegion(String id, double latitude, double longitude, float radius) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
    }

    public PrivateRegion(double latitude, double longitude, float radius) {
        this.id = UUID.randomUUID().toString();
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
    }

    public String getId() {
        return id;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public float getRadius() {
        return radius;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("latitude", latitude);
        json.put("longitude", longitude);
        json.put("radius", radius);
        return json;
    }

    public static PrivateRegion fromJson(JSONObject json) throws JSONException {
        String id = json.getString("id");
        double latitude = json.getDouble("latitude");
        double longitude = json.getDouble("longitude");
        float radius = (float) json.getDouble("radius");
        return new PrivateRegion(id, latitude, longitude, radius);
    }

    public static List<PrivateRegion> getSavedRegions(SharedPreferences sharedPreferences) {
        List<PrivateRegion> regions = new ArrayList<>();
        String regionsJsonStr = sharedPreferences.getString(SharedPrefsKeys.PRIVATE_REGIONS, null);
        if (regionsJsonStr == null || regionsJsonStr.isEmpty()) {
            return regions;
        }
        try {
            JSONArray jsonArray = new JSONArray(regionsJsonStr);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                regions.add(fromJson(obj));
            }
        } catch (JSONException e) {
            Timber.e(e);
        }
        return regions;
    }

    public static void saveRegions(SharedPreferences sharedPreferences, List<PrivateRegion> regions) {
        try {
            JSONArray jsonArray = new JSONArray();
            for (PrivateRegion region : regions) {
                jsonArray.put(region.toJson());
            }
            sharedPreferences.edit().putString(SharedPrefsKeys.PRIVATE_REGIONS, jsonArray.toString()).apply();
        } catch (JSONException e) {
            Timber.e(e);
        }
    }

    public static boolean isEffectiveGhostModeActive(SharedPreferences sharedPreferences, OwnLocationModel ownLocationModel) {
        boolean isObserverModeActive = new BooleanPreference(sharedPreferences, SharedPrefsKeys.OBSERVER_MODE_ACTIVE).get();
        if (isObserverModeActive) {
            return true;
        }
        if (ownLocationModel.ownLocation == null) {
            return false;
        }
        List<PrivateRegion> regions = getSavedRegions(sharedPreferences);
        for (PrivateRegion region : regions) {
            float[] results = new float[1];
            Location.distanceBetween(
                    ownLocationModel.ownLocation.getLatitude(),
                    ownLocationModel.ownLocation.getLongitude(),
                    region.getLatitude(),
                    region.getLongitude(),
                    results
            );
            if (results[0] <= region.getRadius()) {
                return true;
            }
        }
        return false;
    }
}
