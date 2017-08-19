package de.stephanlindauer.criticalmaps.utils;

import android.app.Activity;
import android.view.ViewGroup;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import de.stephanlindauer.criticalmaps.BuildConfig;
import de.stephanlindauer.criticalmaps.R;

import static android.support.v4.content.ContextCompat.getColor;

public class MapViewUtils {
    private MapViewUtils() {}

    public static MapView createMapView(Activity activity) {

        Configuration.getInstance().setMapViewHardwareAccelerated(true);
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID + "/"
                + BuildConfig.VERSION_NAME + " " + org.osmdroid.library.BuildConfig.APPLICATION_ID
                + "/" + org.osmdroid.library.BuildConfig.VERSION_NAME);
        MapView mapView = new MapView(activity);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(1);
        mapView.getController().setCenter(new GeoPoint(0.0d, 0.0d));
        mapView.setClickable(true);
        mapView.setBuiltInZoomControls(true);
        mapView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        mapView.setTilesScaledToDpi(true);
        mapView.getOverlayManager()
                .getTilesOverlay()
                .setLoadingBackgroundColor(getColor(activity, R.color.map_loading_tile_color));
        mapView.getOverlayManager()
                .getTilesOverlay()
                .setLoadingLineColor(getColor(activity, R.color.map_loading_line_color));

        return mapView;
    }
}
