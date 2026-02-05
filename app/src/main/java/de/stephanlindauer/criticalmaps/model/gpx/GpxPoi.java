package de.stephanlindauer.criticalmaps.model.gpx;

import org.maplibre.android.geometry.LatLng;

public class GpxPoi {

    private final String name;
    private final LatLng position;

    public GpxPoi(String name, LatLng position) {
        this.name = name;
        this.position = position;
    }

    public String getName() {
        return name;
    }

    public LatLng getPosition() {
        return position;
    }
}
