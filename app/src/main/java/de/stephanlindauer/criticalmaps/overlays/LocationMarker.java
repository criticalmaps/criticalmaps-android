package de.stephanlindauer.criticalmaps.overlays;

import org.osmdroid.ResourceProxy;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.views.MapView;

public class LocationMarker extends Marker {

    public LocationMarker(MapView mapView, ResourceProxy resourceProxy) {
        super(mapView, resourceProxy);
        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
        setInfoWindow(null);
    }
}
