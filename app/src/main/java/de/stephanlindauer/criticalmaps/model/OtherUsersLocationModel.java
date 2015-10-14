package de.stephanlindauer.criticalmaps.model;

import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.Iterator;

public class OtherUsersLocationModel {

    private ArrayList<GeoPoint> otherUsersLocations = new ArrayList<>();

    //singleton
    private static OtherUsersLocationModel instance;

    private OtherUsersLocationModel() {}

    public static OtherUsersLocationModel getInstance() {
        if (OtherUsersLocationModel.instance == null) {
            OtherUsersLocationModel.instance = new OtherUsersLocationModel();
        }
        return OtherUsersLocationModel.instance;
    }

    public void setNewJSON(JSONObject jsonObject) throws JSONException {
        otherUsersLocations = new ArrayList<>(jsonObject.length());

        Iterator<String> keys = jsonObject.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            JSONObject value = jsonObject.getJSONObject(key);
            Integer latitude = Integer.parseInt(value.getString("latitude"));
            Integer longitude = Integer.parseInt(value.getString("longitude"));

            otherUsersLocations.add(new GeoPoint(latitude, longitude));
        }
    }

    public ArrayList<GeoPoint> getOtherUsersLocations() {
        return otherUsersLocations;
    }
}
