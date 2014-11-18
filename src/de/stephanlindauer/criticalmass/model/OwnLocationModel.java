package de.stephanlindauer.criticalmass.model;

import org.osmdroid.util.GeoPoint;

public class OwnLocationModel {

    //singleton
    private static OwnLocationModel instance;
    public GeoPoint ownLocation;
    public GeoPoint ownLocationCoarse;
    public boolean isListeningForLocation = false;

    public static OwnLocationModel getInstance() {
        if (OwnLocationModel.instance == null) {
            OwnLocationModel.instance = new OwnLocationModel();
        }
        return OwnLocationModel.instance;
    }


}
