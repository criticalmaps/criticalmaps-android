package de.stephanlindauer.criticalmaps.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.Iterator;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
public class OtherUsersLocationModel {

    private ArrayList<GeoPoint> otherUsersLocations = new ArrayList<>();

    private final UserModel userModel;

    @Inject
    public OtherUsersLocationModel(UserModel userModel) {
        this.userModel = userModel;
    }


    public void setFromJson(JSONArray jsonArray) throws JSONException {
        otherUsersLocations = new ArrayList<>(jsonArray.length());
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject locationObject = jsonArray.getJSONObject(i);
            if (locationObject.getString("device").equals(userModel.getChangingDeviceToken())) {
                continue; // Ignore own location
            }
            int latitudeE6 = Integer.parseInt(locationObject.getString("latitude"));
            int longitudeE6 = Integer.parseInt(locationObject.getString("longitude"));

            otherUsersLocations.add(
                    new GeoPoint(latitudeE6 / 1000000.0D, longitudeE6 / 1000000.0D));
        }
    }

    public ArrayList<GeoPoint> getOtherUsersLocations() {
        return otherUsersLocations;
    }
}
