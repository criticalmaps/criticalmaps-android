package de.stephanlindauer.criticalmaps.handler;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.widget.Toast;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;
import javax.xml.parsers.ParserConfigurationException;

import de.stephanlindauer.criticalmaps.App;
import de.stephanlindauer.criticalmaps.R;
import de.stephanlindauer.criticalmaps.model.gpx.GpxModel;
import de.stephanlindauer.criticalmaps.model.gpx.GpxPoi;
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

    public void showGpx(MapView mapView) {
        boolean showTrack = new BooleanPreference(sharedPreferences, SharedPrefsKeys.SHOW_GPX).get();
        if (!showTrack) {
            return;
        }

        String gpxUri = new StringPreference(sharedPreferences, SharedPrefsKeys.GPX_FILE).get();
        if (gpxModel.getUri() == null || !gpxModel.getUri().equals(gpxUri)) {
            readFile(gpxUri);
        }

        showModelOnMap(mapView);
    }

    private void readFile(String gpxUri) {
        try {
            InputStream gpxInputStream = app.getContentResolver().openInputStream(Uri.parse(gpxUri));
            gpxReader.readDataFromStream(gpxInputStream, gpxUri);
        } catch (SecurityException | IOException | SAXException | ParserConfigurationException e) {
            Toast.makeText(app, R.string.gpx_reading_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void showModelOnMap(MapView mapView) {
        for (GpxTrack track : gpxModel.getTracks()) {
            addTrackToMap(mapView, track);
        }

        for (GpxPoi poi : gpxModel.getPoiList()) {
            addPoiToMap(mapView, poi);
        }
    }

    private void addTrackToMap(MapView mapView, GpxTrack track) {
        Polyline trackLine = new Polyline(mapView);
        trackLine.setPoints(track.getWaypoints());
        trackLine.setTitle(track.getName());
        trackLine.getOutlinePaint().setColor(Color.RED);
        mapView.getOverlayManager().add(trackLine);
    }

    private void addPoiToMap(MapView mapView, GpxPoi poi) {
        Marker marker = new Marker(mapView);
        marker.setPosition(poi.getPosition());
        marker.setTitle(poi.getName());
        mapView.getOverlayManager().add(marker);
    }
}