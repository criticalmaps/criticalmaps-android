package de.stephanlindauer.criticalmaps.model;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;
import org.maplibre.android.geometry.LatLng;

import javax.inject.Inject;
import javax.inject.Singleton;

import timber.log.Timber;

@Singleton
public class OwnLocationModel {

    private static final float ACCURACY_PRECISE_THRESHOLD = 50.0f; //meters

    public LatLng ownLocation;
    private boolean isLocationPrecise;

    @Inject
    public OwnLocationModel() {
    }

    public void setLocation(@NonNull LatLng location, float accuracy) {
        ownLocation = location;
        isLocationPrecise = accuracy < ACCURACY_PRECISE_THRESHOLD;
    }

    public boolean hasPreciseLocation() {
        return ownLocation != null && isLocationPrecise;
    }

    @NonNull
    public JSONObject getLocationJson() {
        JSONObject locationObject = new JSONObject();
        try {
            locationObject.put("longitude", Integer.toString((int) (ownLocation.getLongitude() * 1000000.0D)));
            locationObject.put("latitude", Integer.toString((int) (ownLocation.getLatitude() * 1000000.0D)));
        } catch (JSONException e) {
            Timber.e(e);
        }
        return locationObject;
    }
}
