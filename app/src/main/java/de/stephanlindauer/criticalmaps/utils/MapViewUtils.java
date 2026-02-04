package de.stephanlindauer.criticalmaps.utils;

import static org.maplibre.android.style.layers.Property.LINE_CAP_ROUND;
import static org.maplibre.android.style.layers.Property.TEXT_ANCHOR_BOTTOM_RIGHT;

import android.app.Activity;
import android.graphics.Color;
import android.view.Gravity;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;

import org.maplibre.android.camera.CameraPosition;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.maps.MapLibreMapOptions;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.Style;
import org.maplibre.android.module.http.HttpRequestUtil;
import org.maplibre.android.style.layers.Layer;
import org.maplibre.android.style.layers.LineLayer;
import org.maplibre.android.style.layers.PropertyFactory;
import org.maplibre.android.style.layers.SymbolLayer;
import org.maplibre.android.style.sources.GeoJsonSource;

import de.stephanlindauer.criticalmaps.App;
import de.stephanlindauer.criticalmaps.R;

public class MapViewUtils {
    private MapViewUtils() {
    }

    public static int dpToInt(int dp) {
        return Math.round(dp * App.components().app().getResources().getDisplayMetrics().density);
    }

    public static MapView createMapView(Activity activity) {
        HttpRequestUtil.setOkHttpClient(App.components().okHttpClient());

        MapLibreMapOptions options = MapLibreMapOptions.createFromAttributes(activity);

        options.attributionEnabled(false);
        options.logoEnabled(false);
        options.foregroundLoadColor(ContextCompat.getColor(activity, R.color.map_loading_tile_color));

        options.compassEnabled(true);
        options.compassFadesWhenFacingNorth(true);
        options.compassMargins(new int[]{dpToInt(18), dpToInt(18), 0, 0}); // LTRB
        options.compassGravity(Gravity.TOP | GravityCompat.START);
        options.compassImage(AppCompatResources.getDrawable(activity, R.drawable.ic_map_compass));

        options.camera(
                new CameraPosition.Builder()
                        .target(new LatLng(0, 0))
                        .bearing(0.0)
                        .zoom(1.0)
                        .tilt(0.0)
                        .build()
        );

        return new MapView(activity, options);
    }

    public static void setupSourcesAndLayers(Activity activity, Style mapStyle) {

        GeoJsonSource gpxTrackSource = new GeoJsonSource("gpxTrackSource");
        GeoJsonSource gpxPoiSource = new GeoJsonSource("gpxPoiSource");
        GeoJsonSource otherUsersLocationsSource = new GeoJsonSource("otherUsersLocationsSource");
        GeoJsonSource ownUserLocationSource = new GeoJsonSource("ownUserLocationSource");
        GeoJsonSource ownUserLocationSourceObserver = new GeoJsonSource("ownUserLocationSourceObserver");

        mapStyle.addImage(
                "gpxPoi",
                AppCompatResources.getDrawable(activity, R.drawable.ic_location)); //FIXME Icon
        mapStyle.addImage(
                "otherUser",
                AppCompatResources.getDrawable(activity, R.drawable.ic_map_marker));
        mapStyle.addImage(
                "ownUser",
                AppCompatResources.getDrawable(activity, R.drawable.ic_map_marker_own));
        mapStyle.addImage(
                "ownUserObserver",
                AppCompatResources.getDrawable(activity, R.drawable.ic_map_marker_observer));

        Layer gpxTrackLayer =
                new LineLayer("gpxTrackLayer", gpxTrackSource.getId());
        gpxTrackLayer.setProperties(
                PropertyFactory.lineWidth(6f),
                PropertyFactory.lineCap(LINE_CAP_ROUND),
                PropertyFactory.lineColor(Color.RED)
        );

        Layer gpxPoiLayer =
                new SymbolLayer("gpxPoiLayer", gpxPoiSource.getId());
        gpxPoiLayer.setProperties(
                PropertyFactory.iconImage("gpxPoi"),
                PropertyFactory.iconAllowOverlap(true),
                PropertyFactory.iconIgnorePlacement(true),
                PropertyFactory.iconOptional(false),
                PropertyFactory.textFont(new String[]{"noto_sans_regular"}),
                PropertyFactory.textSize(10f),
                PropertyFactory.textAnchor(TEXT_ANCHOR_BOTTOM_RIGHT),
                PropertyFactory.textField("{label}")
        );

        Layer otherUsersLocationsLayer =
                new SymbolLayer("otherUsersLocationsLayer", otherUsersLocationsSource.getId());
        otherUsersLocationsLayer.setProperties(
                PropertyFactory.iconImage("otherUser"),
                PropertyFactory.iconAllowOverlap(true),
                PropertyFactory.iconIgnorePlacement(true));

        Layer ownUserLocationLayer =
                new SymbolLayer("ownUserLocationLayer", ownUserLocationSource.getId());
        ownUserLocationLayer.setProperties(
                PropertyFactory.iconImage("ownUser"),
                PropertyFactory.iconAllowOverlap(true),
                PropertyFactory.iconIgnorePlacement(true));

        Layer ownUserLocationLayerObserver =
                new SymbolLayer("ownUserLocationLayerObserver", ownUserLocationSourceObserver.getId());
        ownUserLocationLayerObserver.setProperties(
                PropertyFactory.iconImage("ownUserObserver"),
                PropertyFactory.iconAllowOverlap(true),
                PropertyFactory.iconIgnorePlacement(true));

        mapStyle.addLayer(gpxTrackLayer);
        mapStyle.addLayer(gpxPoiLayer);
        mapStyle.addLayer(otherUsersLocationsLayer);
        mapStyle.addLayer(ownUserLocationLayer);
        mapStyle.addLayer(ownUserLocationLayerObserver);
        mapStyle.addSource(gpxTrackSource);
        mapStyle.addSource(gpxPoiSource);
        mapStyle.addSource(otherUsersLocationsSource);
        mapStyle.addSource(ownUserLocationSource);
        mapStyle.addSource(ownUserLocationSourceObserver);
    }

    /*
    public static InfoWindow createObserverInfoWindow(MapView mapView) {
        return new InfoWindow(R.layout.view_observer_infowindow, mapView) {
            @Override
            public void onOpen(Object item) {
                mView.setOnClickListener(v -> close());
            }

            @Override
            public void onClose() {

            }
        };
    }
    */
}
