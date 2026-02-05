package de.stephanlindauer.criticalmaps.model.gpx;

import org.maplibre.android.geometry.LatLng;

import java.util.List;

public class GpxTrack {

    private final String name;
    private final List<LatLng> waypoints;

    public GpxTrack(String name, List<LatLng> waypoints) {
        this.name = name;
        this.waypoints = waypoints;
    }

    public String getName() {
        return name;
    }

    public List<LatLng> getWaypoints() {
        return waypoints;
    }
}
