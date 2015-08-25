package de.stephanlindauer.criticalmaps.service;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import org.osmdroid.util.GeoPoint;

import java.util.Date;

import de.stephanlindauer.criticalmaps.events.NewLocationEvent;
import de.stephanlindauer.criticalmaps.model.OwnLocationModel;
import de.stephanlindauer.criticalmaps.notifications.trackinginfo.TrackingInfoNotificationSetter;
import de.stephanlindauer.criticalmaps.utils.DateUtils;
import de.stephanlindauer.criticalmaps.utils.LocationUtils;

public class GPSMananger {

    //dependencies
    private final OwnLocationModel ownLocationModel = OwnLocationModel.getInstance();
    private final EventService eventService = EventService.getInstance();

    //const
    private final float LOCATION_REFRESH_DISTANCE = 20; //20 meters
    private final long LOCATION_REFRESH_TIME = 12 * 1000; //12 seconds

    //misc
    private LocationManager locationManager;
    private SharedPreferences sharedPreferences;

    //singleton
    private static GPSMananger instance;

    public static GPSMananger getInstance() {
        if (GPSMananger.instance == null) {
            GPSMananger.instance = new GPSMananger();
        }
        return GPSMananger.instance;
    }

    public void initialize(Activity activity) {
        locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        startLocationListening();
        sharedPreferences = activity.getPreferences(Context.MODE_PRIVATE);
    }

    private void startLocationListening() {
        if (locationManager.getAllProviders().contains(LocationManager.GPS_PROVIDER) && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME, LOCATION_REFRESH_DISTANCE, locationListener);

        ownLocationModel.isListeningForLocation = true;
        TrackingInfoNotificationSetter.getInstance().show();
    }

    private void stopLocationListening() {
        locationManager.removeUpdates(locationListener);
        ownLocationModel.ownLocation = null;
        ownLocationModel.isListeningForLocation = false;
        TrackingInfoNotificationSetter.getInstance().cancel();
    }

    public GeoPoint getLastKnownLocation() {
        GeoPoint lastKnownLocation = null;
        if (sharedPreferences.contains("latitude") && sharedPreferences.contains("longitude") && sharedPreferences.contains("timestamp")) {
            Date timestampLastCoords = new Date(Long.valueOf(sharedPreferences.getLong("timestamp", 0)));
            if (!DateUtils.isLongerAgoThen5Minutes(timestampLastCoords)) {
                lastKnownLocation = new GeoPoint(
                        Double.parseDouble(sharedPreferences.getString("latitude", "")),
                        Double.parseDouble(sharedPreferences.getString("longitude", "")));
            }
        } else {
            lastKnownLocation = LocationUtils.getBestLastKnownLocation(locationManager);
        }

        return lastKnownLocation;
    }

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            ownLocationModel.ownLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
            eventService.post(new NewLocationEvent());
            sharedPreferences.edit()
                    .putString("latitude", String.valueOf(location.getLatitude()))
                    .putString("longitude", String.valueOf(location.getLongitude()))
                    .putLong("timestamp", new Date().getTime())
                    .commit();
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
        }

        @Override
        public void onProviderEnabled(String s) {
        }

        @Override
        public void onProviderDisabled(String s) {
        }
    };

    public void setTrackingUserLocation(boolean shouldBeTracking) {
        if (shouldBeTracking)
            startLocationListening();
        else
            stopLocationListening();
    }
}
