package de.stephanlindauer.criticalmaps.overlays;

import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.MapView;

public class LocationMarker extends Marker {

    public LocationMarker(MapView mapView) {
        super(mapView);
        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
        setInfoWindow(null);
    }
}
