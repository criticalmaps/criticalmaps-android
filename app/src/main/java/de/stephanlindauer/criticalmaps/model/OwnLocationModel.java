package de.stephanlindauer.criticalmaps.model;

import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import java.util.Date;

import de.stephanlindauer.criticalmaps.utils.DateUtils;

public class OwnLocationModel {

    public static final int MAX_LOCATION_AGE = 30 * 1000; //30 seconds
    private static final float ACCURACY_PRECISE_THRESHOLD = 50.0f; //meters

    public GeoPoint ownLocation;
    private boolean isLocationPrecise;
    private long timeOfFix;

    public void setLocation(@NonNull GeoPoint location, float accuracy, long time) {
        ownLocation = location;
        isLocationPrecise = accuracy < ACCURACY_PRECISE_THRESHOLD;
        timeOfFix = time;
    }

    public boolean hasPreciseLocation() {
        return (ownLocation != null) && isLocationPrecise;
    }

    public boolean isLocationFresh() {
        return DateUtils.isNotLongerAgoThen(new Date(timeOfFix), 0, 30);
    }

    @NonNull
    public JSONObject getLocationJson() {
        JSONObject locationObject = new JSONObject();
        try {
            locationObject.put("longitude", Integer.toString(ownLocation.getLongitudeE6()));
            locationObject.put("latitude", Integer.toString(ownLocation.getLatitudeE6()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return locationObject;
    }
}
