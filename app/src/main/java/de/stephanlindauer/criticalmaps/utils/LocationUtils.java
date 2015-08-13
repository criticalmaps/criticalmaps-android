package de.stephanlindauer.criticalmaps.utils;

import android.location.LocationManager;

import org.osmdroid.util.GeoPoint;

public class LocationUtils {
    public static GeoPoint getBestLastKnownLocation(LocationManager locationManager) {
        GeoPoint lastKnownLocation = null;

        if (locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) {
            lastKnownLocation = new GeoPoint(
                    locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER).getLatitude(),
                    locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER).getLongitude());
        }

        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            lastKnownLocation = new GeoPoint(
                    locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER).getLatitude(),
                    locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER).getLongitude());
        }

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            lastKnownLocation = new GeoPoint(
                    locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLatitude(),
                    locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLongitude());
        }
        return lastKnownLocation;
    }
}
