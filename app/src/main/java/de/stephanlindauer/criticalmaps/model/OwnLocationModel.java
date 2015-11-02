package de.stephanlindauer.criticalmaps.model;

import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

public class OwnLocationModel {

    public GeoPoint ownLocation;

    //singleton
    private static OwnLocationModel instance;

    private OwnLocationModel() {}

    public static OwnLocationModel getInstance() {
        if (OwnLocationModel.instance == null) {
            OwnLocationModel.instance = new OwnLocationModel();
        }
        return OwnLocationModel.instance;
    }

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
