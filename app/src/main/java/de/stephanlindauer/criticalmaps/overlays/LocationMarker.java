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

    // The Marker class holds a static reference to a DefaultInfoWindow instance
    // which in turn holds a reference to the MapView. Thus it leaks the entire
    // view hierarchy when the fragment is detached.
    // Work around this by nulling out static references on detach.
    // TODO check if osmbonuspack fixed this
    @Override
    public void onDetach(MapView mapView) {
        mDefaultIcon = null;
        mDefaultInfoWindow = null;
        super.onDetach(mapView);
    }
}
