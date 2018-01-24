package de.stephanlindauer.criticalmaps.model;

import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.Iterator;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class OtherUsersLocationModel {

    private ArrayList<GeoPoint> otherUsersLocations = new ArrayList<>();

    @Inject
    public OtherUsersLocationModel() {
    }

    public void setNewJSON(JSONObject jsonObject) throws JSONException {
        otherUsersLocations = new ArrayList<>(jsonObject.length());

        Iterator<String> keys = jsonObject.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            JSONObject value = jsonObject.getJSONObject(key);
            int latitudeE6 = Integer.parseInt(value.getString("latitude"));
            int longitudeE6 = Integer.parseInt(value.getString("longitude"));

            otherUsersLocations.add(new GeoPoint(latitudeE6 / 1000000.0D, longitudeE6 / 1000000.0D));
        }
    }

    public ArrayList<GeoPoint> getOtherUsersLocations() {
        return otherUsersLocations;
    }
}
