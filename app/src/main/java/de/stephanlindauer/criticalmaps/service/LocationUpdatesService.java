package de.stephanlindauer.criticalmaps.service;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.osmdroid.util.GeoPoint;

import java.util.Date;

import de.stephanlindauer.criticalmaps.events.Events;
import de.stephanlindauer.criticalmaps.model.OwnLocationModel;
import de.stephanlindauer.criticalmaps.provider.EventBusProvider;
import de.stephanlindauer.criticalmaps.utils.DateUtils;
import de.stephanlindauer.criticalmaps.utils.LocationUtils;

public class LocationUpdatesService {

    //dependencies
    private final OwnLocationModel ownLocationModel = OwnLocationModel.getInstance();
    private final EventBusProvider eventService = EventBusProvider.getInstance();

    //const
    private static final float LOCATION_REFRESH_DISTANCE = 20; //20 meters
    private static final long LOCATION_REFRESH_TIME = 12 * 1000; //12 seconds
    private static final int MAX_LOCATION_AGE = 30 * 1000; //30 seconds

    //misc
    private LocationManager locationManager;
    private SharedPreferences sharedPreferences;
    private boolean isRegisteredForLocationUpdates;
    private Location lastPublishedLocation;

    //singleton
    private static LocationUpdatesService instance;

    private LocationUpdatesService() {
    }

    public static LocationUpdatesService getInstance() {
        if (LocationUpdatesService.instance == null) {
            LocationUpdatesService.instance = new LocationUpdatesService();
        }
        return LocationUpdatesService.instance;
    }

    public void initializeAndStartListening(@NonNull Application application) {
        locationManager = (LocationManager) application.getSystemService(Context.LOCATION_SERVICE);
        sharedPreferences = application.getSharedPreferences("Main", Context.MODE_PRIVATE);
        registerLocationListeners();
    }

    private void registerLocationListeners() {
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME, LOCATION_REFRESH_DISTANCE, locationListener);
        }
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_REFRESH_TIME, LOCATION_REFRESH_DISTANCE, locationListener);
        }

        isRegisteredForLocationUpdates = true;
    }

    public void handleShutdown() {
        if (!isRegisteredForLocationUpdates) {
            return;
        }

        locationManager.removeUpdates(locationListener);
        isRegisteredForLocationUpdates = false;
    }

    @Nullable
    public GeoPoint getLastKnownLocation() {
        if (sharedPreferences.contains("latitude") && sharedPreferences.contains("longitude") && sharedPreferences.contains("timestamp")) {
            Date timestampLastCoords = new Date(sharedPreferences.getLong("timestamp", 0));
            if (!DateUtils.isLongerAgoThen5Minutes(timestampLastCoords)) {
                return new GeoPoint(
                        Double.parseDouble(sharedPreferences.getString("latitude", "")),
                        Double.parseDouble(sharedPreferences.getString("longitude", "")));
            }
        } else {
            return LocationUtils.getBestLastKnownLocation(locationManager);
        }
        return null;
    }

    private void publishNewLocation(GeoPoint newLocation) {
        ownLocationModel.ownLocation = newLocation;
        eventService.post(Events.NEW_LOCATION_EVENT);
        sharedPreferences.edit()
                .putString("latitude", String.valueOf(newLocation.getLatitude()))
                .putString("longitude", String.valueOf(newLocation.getLongitude()))
                .putLong("timestamp", new Date().getTime())
                .apply();
    }

    private boolean shouldPublishNewLocation(Location location) {
        // Any location is better than no location
        if (lastPublishedLocation == null) {
            return true;
        }

        // Average speed of the CM is ~4 m/s so anything over 30 seconds old, may already
        // be well over 120m off. So a newer fix is assumed to be always better.
        long timeDelta = location.getTime() - lastPublishedLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > MAX_LOCATION_AGE;
        boolean isSignificantlyOlder = timeDelta < -MAX_LOCATION_AGE;
        boolean isNewer = timeDelta > 0;

        if (isSignificantlyNewer) {
            return true;
        } else if (isSignificantlyOlder) {
            return false;
        }

        int accuracyDelta = (int) (location.getAccuracy() - lastPublishedLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 120;

        boolean isFromSameProvider = location.getProvider().equals(lastPublishedLocation.getProvider());

        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }

        return false;
    }

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            if (shouldPublishNewLocation(location)) {
                publishNewLocation(new GeoPoint(location.getLatitude(), location.getLongitude()));
                lastPublishedLocation = location;
            }
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
