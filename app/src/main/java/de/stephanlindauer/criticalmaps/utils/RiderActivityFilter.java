package de.stephanlindauer.criticalmaps.utils;

import android.location.Location;

import org.maplibre.android.geometry.LatLng;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RiderActivityFilter {

    private static final double SHORT_RANGE_METERS = 250.0;
    private static final double LONG_RANGE_METERS  = 8_000.0;

    private RiderActivityFilter() {}

    public static Set<String> classify(Map<String, LatLng> locations) {
        Set<String> activeIds = new HashSet<>();
        List<Map.Entry<String, LatLng>> entries = new java.util.ArrayList<>(locations.entrySet());

        for (Map.Entry<String, LatLng> entry : entries) {
            int shortRange = 0;
            int longRange  = 0;

            for (Map.Entry<String, LatLng> other : entries) {
                if (other.getKey().equals(entry.getKey())) continue;

                float distance = distanceMeters(entry.getValue(), other.getValue());
                if (distance <= LONG_RANGE_METERS)  longRange++;
                if (distance <= SHORT_RANGE_METERS) shortRange++;
            }

            if (isActive(shortRange, longRange)) {
                activeIds.add(entry.getKey());
            }
        }

        return activeIds;
    }

    private static boolean isActive(int shortRange, int longRange) {
        return shortRange >= 3 || shortRange >= 2 && longRange < 15;
    }

    private static float distanceMeters(LatLng a, LatLng b) {
        float[] result = new float[1];
        Location.distanceBetween(
            a.getLatitude(), a.getLongitude(),
            b.getLatitude(), b.getLongitude(),
            result
        );
        return result[0];
    }
}