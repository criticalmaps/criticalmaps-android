package de.stephanlindauer.criticalmaps.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.maplibre.android.geometry.LatLng;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class OtherUsersLocationModel {

    private final Map<String, LatLng> otherUsersLocations = new HashMap<>();

    private final UserModel userModel;

    @Inject
    public OtherUsersLocationModel(UserModel userModel) {
        this.userModel = userModel;
    }


    public void setFromJson(JSONArray jsonArray) throws JSONException {
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject locationObject = jsonArray.getJSONObject(i);
            if (locationObject.getString("device").equals(userModel.getChangingDeviceToken())) {
                continue; // Ignore own location
            }
            String deviceId = locationObject.getString("device");
            int latitudeE6 = Integer.parseInt(locationObject.getString("latitude"));
            int longitudeE6 = Integer.parseInt(locationObject.getString("longitude"));

            otherUsersLocations.put(deviceId, new LatLng(latitudeE6 / 1E6, longitudeE6 / 1E6));
        }
    }

    public Map<String, LatLng> getOtherUsersLocations() {
        return otherUsersLocations;
    }
}
