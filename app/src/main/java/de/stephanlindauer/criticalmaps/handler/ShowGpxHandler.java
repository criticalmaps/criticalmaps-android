package de.stephanlindauer.criticalmaps.handler;

import android.content.SharedPreferences;
import android.net.Uri;
import android.widget.Toast;

import com.google.gson.JsonObject;

import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.maps.Style;
import org.maplibre.android.style.sources.GeoJsonSource;

import org.maplibre.geojson.Feature;
import org.maplibre.geojson.FeatureCollection;
import org.maplibre.geojson.LineString;
import org.maplibre.geojson.Point;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.inject.Inject;
import javax.xml.parsers.ParserConfigurationException;

import de.stephanlindauer.criticalmaps.App;
import de.stephanlindauer.criticalmaps.R;
import de.stephanlindauer.criticalmaps.model.gpx.GpxModel;
import de.stephanlindauer.criticalmaps.model.gpx.GpxTrack;
import de.stephanlindauer.criticalmaps.prefs.SharedPrefsKeys;
import de.stephanlindauer.criticalmaps.utils.GpxReader;
import info.metadude.android.typedpreferences.BooleanPreference;
import info.metadude.android.typedpreferences.StringPreference;

public class ShowGpxHandler {

    private final SharedPreferences sharedPreferences;
    private final GpxModel gpxModel;
    private final App app;
    private final GpxReader gpxReader;


    @Inject
    public ShowGpxHandler(SharedPreferences sharedPreferences, GpxModel gpxModel, App app, GpxReader gpxReader) {
        this.sharedPreferences = sharedPreferences;
        this.gpxModel = gpxModel;
        this.app = app;
        this.gpxReader = gpxReader;
    }

    public void showGpx(Style mapStyle) {
        boolean showTrack = new BooleanPreference(sharedPreferences, SharedPrefsKeys.SHOW_GPX).get();
        if (!showTrack) {
            return;
        }

        String gpxUri = new StringPreference(sharedPreferences, SharedPrefsKeys.GPX_FILE).get();
        if (gpxModel.getUri() == null || !gpxModel.getUri().equals(gpxUri)) {
            readFile(gpxUri);
        }

        showModelOnMap(mapStyle);
    }

    private void readFile(String gpxUri) {
        try {
            InputStream gpxInputStream = app.getContentResolver().openInputStream(Uri.parse(gpxUri));
            gpxReader.readDataFromStream(gpxInputStream, gpxUri);
        } catch (SecurityException | IOException | SAXException | ParserConfigurationException e) {
            Toast.makeText(app, R.string.gpx_reading_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void showModelOnMap(Style mapStyle) {
        addTracksToMap(mapStyle);
        addPoisToMap(mapStyle);
    }

    private void addTracksToMap(Style mapStyle) {
        ArrayList<Feature> features = new ArrayList<>();

        for (GpxTrack track : gpxModel.getTracks()) {
            ArrayList<Point> points = new ArrayList<>();

            for (LatLng location : track.getWaypoints()) {
                points.add(Point.fromLngLat(location.getLongitude(), location.getLatitude()));
            }

            JsonObject properties = new JsonObject();
            properties.addProperty("label", track.getName());
            features.add(Feature.fromGeometry(LineString.fromLngLats(points), properties));
        }

        GeoJsonSource gpxTrackSource =
                (GeoJsonSource) mapStyle.getSource("gpxTrackSource");
        gpxTrackSource.setGeoJson(FeatureCollection.fromFeatures(features));
    }

    private void addPoisToMap(Style mapStyle) {
        ArrayList<Feature> features = new ArrayList<>();

        gpxModel.getPoiList().forEach((gpxPoi) -> {
            LatLng location = gpxPoi.getPosition();
            JsonObject properties = new JsonObject();
            properties.addProperty("label", gpxPoi.getName());
            features.add(Feature.fromGeometry(
                    Point.fromLngLat(location.getLongitude(), location.getLatitude()), properties));
        });

        GeoJsonSource gpxPoiSource =
                (GeoJsonSource) mapStyle.getSource("gpxPoiSource");
        gpxPoiSource.setGeoJson(FeatureCollection.fromFeatures(features));
    }
}
