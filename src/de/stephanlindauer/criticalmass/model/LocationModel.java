package de.stephanlindauer.criticalmass.model;

import org.osmdroid.util.GeoPoint;

public class LocationModel {

    private static LocationModel instance;

    public GeoPoint ownLocation;

    public static LocationModel getInstance() {
        if (LocationModel.instance == null) {
            LocationModel.instance = new LocationModel();
        }
        return LocationModel.instance;
    }


}
