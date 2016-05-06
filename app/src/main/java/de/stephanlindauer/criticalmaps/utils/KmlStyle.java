package de.stephanlindauer.criticalmaps.utils;

import android.app.Activity;

import org.osmdroid.bonuspack.kml.KmlFeature;
import org.osmdroid.bonuspack.kml.KmlLineString;
import org.osmdroid.bonuspack.kml.KmlPlacemark;
import org.osmdroid.bonuspack.kml.KmlPoint;
import org.osmdroid.bonuspack.kml.KmlPolygon;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.bonuspack.overlays.Polygon;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.views.overlay.Overlay;

import de.stephanlindauer.criticalmaps.R;

public class KmlStyle implements KmlFeature.Styler {
    Activity activity;

    public KmlStyle(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void onFeature(Overlay overlay, KmlFeature kmlFeature) {

    }

    @Override
    public void onPoint(Marker marker, KmlPlacemark kmlPlacemark, KmlPoint kmlPoint) {

    }

    @Override
    public void onLineString(Polyline polyline, KmlPlacemark kmlPlacemark, KmlLineString kmlLineString) {
        polyline.setWidth(12.0f);
        polyline.setColor(activity.getResources().getColor(R.color.colorPrimaryDark));
    }

    @Override
    public void onPolygon(Polygon polygon, KmlPlacemark kmlPlacemark, KmlPolygon kmlPolygon) {

    }
}
