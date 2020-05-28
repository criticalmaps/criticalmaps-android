package de.stephanlindauer.criticalmaps.model.gpx;

import org.osmdroid.util.GeoPoint;

import java.util.List;

public class GpxTrack {

    private String name;
    private List<GeoPoint> waypoints;

    public GpxTrack(String name, List<GeoPoint> waypoints) {
        this.name = name;
        this.waypoints = waypoints;
    }

    public String getName() {
        return name;
    }

    public List<GeoPoint> getWaypoints() {
        return waypoints;
    }
}
