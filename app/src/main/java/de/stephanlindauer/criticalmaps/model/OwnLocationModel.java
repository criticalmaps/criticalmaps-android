package de.stephanlindauer.criticalmaps.model;

import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import java.util.Date;

import javax.inject.Inject;
import javax.inject.Singleton;

import de.stephanlindauer.criticalmaps.utils.DateUtils;

@Singleton
public class OwnLocationModel {

    public static final int MAX_LOCATION_AGE = 30 * 1000; //30 seconds
    private static final float ACCURACY_PRECISE_THRESHOLD = 50.0f; //meters

    public GeoPoint ownLocation;
    private boolean isLocationPrecise;
    private long timeOfFix;

    @Inject
    public OwnLocationModel() {
    }

    public void setLocation(@NonNull GeoPoint location, float accuracy, long timestamp) {
        ownLocation = location;
        isLocationPrecise = accuracy < ACCURACY_PRECISE_THRESHOLD;
        timeOfFix = timestamp;
    }

    public boolean hasPreciseLocation() {
        return ownLocation != null && isLocationPrecise;
    }

    public boolean isLocationFresh() {
        return DateUtils.isNotLongerAgoThen(new Date(timeOfFix), 0, 30);
    }

    @NonNull
    public JSONObject getLocationJson() {
        JSONObject locationObject = new JSONObject();
        try {
            locationObject.put("longitude", Integer.toString((int)(ownLocation.getLongitude() * 1000000.0D)));
            locationObject.put("latitude", Integer.toString((int)(ownLocation.getLatitude() * 1000000.0D)));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return locationObject;
    }
}
