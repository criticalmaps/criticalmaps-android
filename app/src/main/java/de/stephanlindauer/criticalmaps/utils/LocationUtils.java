package de.stephanlindauer.criticalmaps.utils;

import android.location.Location;
import android.location.LocationManager;

import org.osmdroid.util.GeoPoint;

public class LocationUtils {
    public static GeoPoint getBestLastKnownLocation(LocationManager locationManager) {
        if (locationManager.getAllProviders().contains(LocationManager.GPS_PROVIDER) && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Location lastKnownLocationGps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLocationGps != null) {
                return new GeoPoint(
                        lastKnownLocationGps.getLatitude(),
                        lastKnownLocationGps.getLongitude());
            }
        }

        if (locationManager.getAllProviders().contains(LocationManager.NETWORK_PROVIDER) && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            Location lastKnownLocationNetwork = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (lastKnownLocationNetwork != null) {
                return new GeoPoint(
                        lastKnownLocationNetwork.getLatitude(),
                        lastKnownLocationNetwork.getLongitude());
            }
        }

        if (locationManager.getAllProviders().contains(LocationManager.PASSIVE_PROVIDER) && locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) {
            Location lastKnownLocationPassive = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            if (lastKnownLocationPassive != null) {
                return new GeoPoint(
                        lastKnownLocationPassive.getLatitude(),
                        lastKnownLocationPassive.getLongitude());
            }
        }

        return null;
    }
}
