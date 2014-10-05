package de.stephanlindauer.criticalmass.model;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;

public class OtherUsersLocationModel {
    private static OtherUsersLocationModel instance;
    public ArrayList<GeoPoint> otherUsersLocations;

    public static OtherUsersLocationModel getInstance() {
        if (OtherUsersLocationModel.instance == null) {
            OtherUsersLocationModel.instance = new OtherUsersLocationModel();
        }
        return OtherUsersLocationModel.instance;
    }
}
