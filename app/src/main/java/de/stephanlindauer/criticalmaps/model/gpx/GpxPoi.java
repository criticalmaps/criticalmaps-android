package de.stephanlindauer.criticalmaps.model.gpx;

import org.osmdroid.util.GeoPoint;

public class GpxPoi {

    private String name;
    private GeoPoint position;

    public GpxPoi(String name, GeoPoint position) {
        this.name = name;
        this.position = position;
    }

    public String getName() {
        return name;
    }

    public GeoPoint getPosition() {
        return position;
    }
}
