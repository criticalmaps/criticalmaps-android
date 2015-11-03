package de.stephanlindauer.criticalmaps.model;

import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

public class OwnLocationModel {

    private static final float ACCURACY_PRECISE_THRESHOLD = 50.0f; //meters

    public GeoPoint ownLocation;
    private boolean isLocationPrecise;

    //singleton
    private static OwnLocationModel instance;

    private OwnLocationModel() {}

    public static OwnLocationModel getInstance() {
        if (OwnLocationModel.instance == null) {
            OwnLocationModel.instance = new OwnLocationModel();
        }
        return OwnLocationModel.instance;
    }

    public void setLocation(@NonNull GeoPoint location, float accuracy) {
        ownLocation = location;
        isLocationPrecise = (accuracy < ACCURACY_PRECISE_THRESHOLD);
    }

    public boolean hasPreciseLocation() {
        return (ownLocation != null) && isLocationPrecise;
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
