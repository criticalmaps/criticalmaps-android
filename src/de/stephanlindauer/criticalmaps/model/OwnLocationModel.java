package de.stephanlindauer.criticalmaps.model;

import org.osmdroid.util.GeoPoint;

public class OwnLocationModel {

    public GeoPoint ownLocation;
    public GeoPoint ownLocationCoarse;

    public boolean isListeningForLocation = false;

    //singleton
    private static OwnLocationModel instance;

    public static OwnLocationModel getInstance() {
        if (OwnLocationModel.instance == null) {
            OwnLocationModel.instance = new OwnLocationModel();
        }
        return OwnLocationModel.instance;
    }


}
