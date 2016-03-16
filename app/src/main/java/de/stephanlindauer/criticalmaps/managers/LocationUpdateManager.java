package de.stephanlindauer.criticalmaps.managers;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;

import org.osmdroid.util.GeoPoint;

import java.util.Date;

import javax.inject.Inject;

import de.stephanlindauer.criticalmaps.App;
import de.stephanlindauer.criticalmaps.events.Events;
import de.stephanlindauer.criticalmaps.model.OwnLocationModel;
import de.stephanlindauer.criticalmaps.prefs.GeoPointPreference;
import de.stephanlindauer.criticalmaps.provider.EventBusProvider;
import de.stephanlindauer.criticalmaps.utils.DateUtils;
import de.stephanlindauer.criticalmaps.utils.LocationUtils;
import de.stephanlindauer.criticalmaps.prefs.SharedPrefsKeys;
import info.metadude.android.typedpreferences.LongPreference;

public class LocationUpdateManager {

    private final OwnLocationModel ownLocationModel;
    private final EventBusProvider eventService;

    //const
    private static final float LOCATION_REFRESH_DISTANCE = 20; //20 meters
    private static final long LOCATION_REFRESH_TIME = 12 * 1000L; //12 seconds

    //misc
    private LocationManager locationManager;
    private SharedPreferences sharedPreferences;
    private GeoPointPreference lastKnownLocationPreference;
    private LongPreference timeStampPreference;
    private boolean isRegisteredForLocationUpdates;
    private Location lastPublishedLocation;
    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            if (shouldPublishNewLocation(location)) {
                publishNewLocation(location);
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

    @Inject
    public LocationUpdateManager(App app,
                                 OwnLocationModel ownLocationModel,
                                 EventBusProvider eventService,
                                 SharedPreferences sharedPreferences) {
        this.ownLocationModel = ownLocationModel;
        this.eventService = eventService;
        this.sharedPreferences = sharedPreferences;
        locationManager = (LocationManager) app.getSystemService(Context.LOCATION_SERVICE);
    }


    public void initializeAndStartListening() {
        lastKnownLocationPreference = new GeoPointPreference(
                sharedPreferences, SharedPrefsKeys.LAST_KNOWN_LOCATION);
        timeStampPreference = new LongPreference(
                sharedPreferences, SharedPrefsKeys.TIME_STAMP);
        registerLocationListeners();
    }

    private void registerLocationListeners() {
        requestLocationUpdatesIfPossible(LocationManager.GPS_PROVIDER);
        requestLocationUpdatesIfPossible(LocationManager.NETWORK_PROVIDER);

        isRegisteredForLocationUpdates = true;
    }

    private void requestLocationUpdatesIfPossible(String provider) {
        if (locationManager.isProviderEnabled(provider)) {
            locationManager.requestLocationUpdates(provider, LOCATION_REFRESH_TIME, LOCATION_REFRESH_DISTANCE, locationListener);
        }
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
        if (lastKnownLocationPreference == null || timeStampPreference == null) {
            return null;
        }
        if (lastKnownLocationPreference.isSet() && timeStampPreference.isSet()) {
            Date timeStampLastCoords = new Date(timeStampPreference.get());
            if (DateUtils.isNotLongerAgoThen(timeStampLastCoords, 5, 0)) {
                return lastKnownLocationPreference.get();
            }
        } else {
            return LocationUtils.getBestLastKnownLocation(locationManager);
        }
        return null;
    }

    private void publishNewLocation(Location location) {
        GeoPoint newLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
        ownLocationModel.setLocation(newLocation, location.getAccuracy(), location.getTime());
        eventService.post(Events.NEW_LOCATION_EVENT);
        lastKnownLocationPreference.set(newLocation);
        timeStampPreference.set(location.getTime());
    }

    private boolean shouldPublishNewLocation(Location location) {
        // Any location is better than no location
        if (lastPublishedLocation == null) {
            return true;
        }

        // Average speed of the CM is ~4 m/s so anything over 30 seconds old, may already
        // be well over 120m off. So a newer fix is assumed to be always better.
        long timeDelta = location.getTime() - lastPublishedLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > OwnLocationModel.MAX_LOCATION_AGE;
        boolean isSignificantlyOlder = timeDelta < -OwnLocationModel.MAX_LOCATION_AGE;
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

}
