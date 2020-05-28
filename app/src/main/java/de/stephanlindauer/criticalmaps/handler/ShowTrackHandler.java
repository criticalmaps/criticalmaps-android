package de.stephanlindauer.criticalmaps.handler;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.widget.Toast;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polyline;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Objects;

import javax.inject.Inject;

import de.stephanlindauer.criticalmaps.App;
import de.stephanlindauer.criticalmaps.model.gpx.GpxModel;
import de.stephanlindauer.criticalmaps.model.gpx.GpxTrack;
import de.stephanlindauer.criticalmaps.prefs.SharedPrefsKeys;
import de.stephanlindauer.criticalmaps.utils.GpxReader;
import info.metadude.android.typedpreferences.BooleanPreference;
import info.metadude.android.typedpreferences.StringPreference;

public class ShowTrackHandler {

    SharedPreferences sharedPreferences;
    GpxModel gpxModel;
    App app;
    PermissionCheckHandler permissionCheckHandler;


    @Inject
    public ShowTrackHandler(SharedPreferences sharedPreferences, GpxModel gpxModel, App app, PermissionCheckHandler permissionCheckHandler) {
        this.sharedPreferences = sharedPreferences;
        this.gpxModel = gpxModel;
        this.app = app;
        this.permissionCheckHandler = permissionCheckHandler;
    }

    public void showGpx(MapView mapView) {
        boolean showTrack = new BooleanPreference(sharedPreferences, SharedPrefsKeys.SHOW_TRACK).get();
        if (!showTrack) {
            return;
        }

        String trackPath = new StringPreference(sharedPreferences, SharedPrefsKeys.TRACK_PATH).get();
        if (!Objects.equals(gpxModel.getUri(), trackPath)) {
            readFile(trackPath);
        }

        for (GpxTrack track : gpxModel.getTracks()) {
            addTrackToMap(mapView, track);
        }
    }

    private void addTrackToMap(MapView mapView, GpxTrack track) {
        Polyline trackLine = new Polyline(mapView);
        trackLine.setPoints(track.getWaypoints());
        trackLine.setTitle(track.getName());
        trackLine.getOutlinePaint().setColor(Color.RED);
        mapView.getOverlayManager().add(trackLine);
    }

    private void readFile(String trackPath) {
        try {
            InputStream gpxInputStream = app.getContentResolver().openInputStream(Uri.parse(trackPath));
            GpxReader.readTrackFromGpx(gpxInputStream, gpxModel, trackPath);
        } catch (FileNotFoundException | SecurityException e) {
            Toast.makeText(app, e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
}