package de.stephanlindauer.criticalmaps.model.gpx;

import org.osmdroid.util.GeoPoint;

public class GpxPoi {

    private final String name;
    private final GeoPoint position;

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
