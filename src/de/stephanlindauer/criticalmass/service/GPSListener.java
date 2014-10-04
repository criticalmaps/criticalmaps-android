package de.stephanlindauer.criticalmass.service;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.location.LocationListener;
import de.stephanlindauer.criticalmass.model.LocationModel;
import de.stephanlindauer.criticalmass.notifications.trackinginfo.TrackingInfoNotificationSetter;
import org.osmdroid.util.GeoPoint;

public class GPSListener {
    private static final float LOCATION_REFRESH_DISTANCE = 30; //meters
    private static final long LOCATION_REFRESH_TIME = 30000; //milliseconds

    private LocationModel locationModel = LocationModel.getInstance();

    private boolean isListeningForLocation = false;

    private LocationManager locationManager;

    public void initialize( Activity mContext  ){
        locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        startLocationListening();
    }

    private void startLocationListening() {
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME,
                LOCATION_REFRESH_DISTANCE, mLocationListener);
        isListeningForLocation = true;
        TrackingInfoNotificationSetter.getInstance().show();
    }

    private void stopLocationListening() {
        locationManager.removeUpdates( mLocationListener);
        locationModel.ownLocation = null;
        isListeningForLocation = false;
        TrackingInfoNotificationSetter.getInstance().cancel();
    }

    public boolean isListeningForLocation() {
        return isListeningForLocation;
    }

    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            LocationModel.getInstance().ownLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
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
}
