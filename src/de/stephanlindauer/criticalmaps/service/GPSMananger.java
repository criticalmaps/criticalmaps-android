package de.stephanlindauer.criticalmaps.service;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import de.stephanlindauer.criticalmaps.events.NewLocationEvent;
import de.stephanlindauer.criticalmaps.model.OwnLocationModel;
import de.stephanlindauer.criticalmaps.notifications.trackinginfo.TrackingInfoNotificationSetter;
import org.osmdroid.util.GeoPoint;

public class GPSMananger {

    //dependencies
    private final OwnLocationModel ownLocationModel = OwnLocationModel.getInstance();
    private final EventService eventService = EventService.getInstance();

    //const
    private final GeoPoint FALLBACK_LOCATION = new GeoPoint((int) (52.520820 * 1E6), (int) (13.409346 * 1E6));
    private final float LOCATION_REFRESH_DISTANCE = 30; //30 meters
    private final long LOCATION_REFRESH_TIME = 30 * 1000; //30 seconds

    //misc
    private LocationManager locationManager;

    //singleton
    private static GPSMananger instance;

    public static GPSMananger getInstance() {
        if (GPSMananger.instance == null) {
            GPSMananger.instance = new GPSMananger();
        }
        return GPSMananger.instance;
    }

    public void initialize(Activity mContext) {
        locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        startLocationListening();
        setLastKnownCoarseLocation();
    }

    private void setLastKnownCoarseLocation() {
        Location lastKnownLocationGPS = null;
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            lastKnownLocationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }

        Location lastKnownLocationNetwork = null;
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            lastKnownLocationNetwork = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        Location lastKnownLocationPassive = null;
        if (locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) {
            lastKnownLocationPassive = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        }

        GeoPoint lastKnownLocation;

        if (lastKnownLocationGPS != null) {
            lastKnownLocation = new GeoPoint(lastKnownLocationGPS.getLatitude(), lastKnownLocationGPS.getLongitude());
        } else if (lastKnownLocationNetwork != null) {
            lastKnownLocation = new GeoPoint(lastKnownLocationNetwork.getLatitude(), lastKnownLocationNetwork.getLongitude());
        } else if (lastKnownLocationPassive != null) {
            lastKnownLocation = new GeoPoint(lastKnownLocationPassive.getLatitude(), lastKnownLocationPassive.getLongitude());
        } else {
            lastKnownLocation = FALLBACK_LOCATION;
        }

        ownLocationModel.ownLocationCoarse = lastKnownLocation;
    }

    private void startLocationListening() {
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME,
                LOCATION_REFRESH_DISTANCE, mLocationListener);
        ownLocationModel.isListeningForLocation = true;
        TrackingInfoNotificationSetter.getInstance().show();
    }

    private void stopLocationListening() {
        locationManager.removeUpdates(mLocationListener);
        ownLocationModel.ownLocation = null;
        ownLocationModel.isListeningForLocation = false;
        TrackingInfoNotificationSetter.getInstance().cancel();
    }

    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            ownLocationModel.ownLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
            ownLocationModel.ownLocationCoarse = new GeoPoint(location.getLatitude(), location.getLongitude());
            eventService.post(new NewLocationEvent());
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
