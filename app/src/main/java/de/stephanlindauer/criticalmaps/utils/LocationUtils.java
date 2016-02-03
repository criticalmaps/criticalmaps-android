package de.stephanlindauer.criticalmaps.utils;

import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.Nullable;
import org.osmdroid.util.GeoPoint;

import static android.location.LocationManager.*;

public class LocationUtils {
    @Nullable
    public static GeoPoint getBestLastKnownLocation(LocationManager locationManager) {
        final String[] providers = new String[]{GPS_PROVIDER, NETWORK_PROVIDER, PASSIVE_PROVIDER};

        for (String provider : providers) {
            if (locationManager.isProviderEnabled(provider)) {
                final Location location = locationManager.getLastKnownLocation(provider);
                if (location != null) {
                    return new GeoPoint(location.getLatitude(), location.getLongitude());
                }
            }
        }

        return null;
    }
}
