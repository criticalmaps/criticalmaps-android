package de.stephanlindauer.criticalmass.service;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import de.stephanlindauer.criticalmass.model.OwnLocationModel;
import de.stephanlindauer.criticalmass.notifications.trackinginfo.TrackingInfoNotificationSetter;
import org.osmdroid.util.GeoPoint;

public class GPSMananger {
    private static final float LOCATION_REFRESH_DISTANCE = 30; //meters
    private static final long LOCATION_REFRESH_TIME = 30000; //milliseconds

    private OwnLocationModel locationModel = OwnLocationModel.getInstance();

    private LocationManager locationManager;

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
    }

    private void startLocationListening() {
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME,
                LOCATION_REFRESH_DISTANCE, mLocationListener);
        locationModel.isListeningForLocation = true;
        TrackingInfoNotificationSetter.getInstance().show();
    }

    private void stopLocationListening() {
        locationManager.removeUpdates(mLocationListener);
        locationModel.ownLocation = null;
        locationModel.isListeningForLocation = false;
        TrackingInfoNotificationSetter.getInstance().cancel();
    }

    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            OwnLocationModel.getInstance().ownLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
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
