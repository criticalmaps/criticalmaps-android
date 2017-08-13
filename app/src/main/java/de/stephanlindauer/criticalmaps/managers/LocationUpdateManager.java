package de.stephanlindauer.criticalmaps.managers;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.squareup.otto.Produce;

import org.osmdroid.util.GeoPoint;

import java.util.List;

import javax.inject.Inject;

import de.stephanlindauer.criticalmaps.App;
import de.stephanlindauer.criticalmaps.events.Events;
import de.stephanlindauer.criticalmaps.events.GpsStatusChangedEvent;
import de.stephanlindauer.criticalmaps.model.OwnLocationModel;
import de.stephanlindauer.criticalmaps.provider.EventBusProvider;

public class LocationUpdateManager {

    private final OwnLocationModel ownLocationModel;
    private final EventBusProvider eventService;

    //const
    private static final float LOCATION_REFRESH_DISTANCE = 20; //20 meters
    private static final long LOCATION_REFRESH_TIME = 12 * 1000; //12 seconds

    //misc
    private LocationManager locationManager;
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
            // Notes after some testing:
            // - seems to be only called for GPS provider
            // - calls not necessarily consistent with location fixes
            // - recurrent AVAILABLE calls for GPS
            // -> not usable
        }

        @Override
        public void onProviderEnabled(String s) {
            postStatusEvent();
        }

        @Override
        public void onProviderDisabled(String s) {
            postStatusEvent();
        }
    };

    @Inject
    public LocationUpdateManager(App app,
                                 OwnLocationModel ownLocationModel,
                                 EventBusProvider eventService) {
        this.ownLocationModel = ownLocationModel;
        this.eventService = eventService;
        locationManager = (LocationManager) app.getSystemService(Context.LOCATION_SERVICE);
        setEventStatus();
        eventService.register(this);
    }

    @Produce
    public GpsStatusChangedEvent produceStatusEvent() {
        return Events.GPS_STATUS_CHANGED_EVENT;
    }

    private void postStatusEvent() {
        setEventStatus();
        eventService.post(Events.GPS_STATUS_CHANGED_EVENT);
    }

    private void setEventStatus() {
        if (locationManager.getProvider(LocationManager.GPS_PROVIDER) == null
                && locationManager.getProvider(LocationManager.NETWORK_PROVIDER) == null) {
            Events.GPS_STATUS_CHANGED_EVENT.status = GpsStatusChangedEvent.Status.NONEXISTENT;
            return;
        }

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Events.GPS_STATUS_CHANGED_EVENT.status = GpsStatusChangedEvent.Status.HIGH_ACCURACY;
        } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            Events.GPS_STATUS_CHANGED_EVENT.status = GpsStatusChangedEvent.Status.LOW_ACCURACY;
        } else {
            Events.GPS_STATUS_CHANGED_EVENT.status = GpsStatusChangedEvent.Status.OFF;
        }
    }

    public void initializeAndStartListening() {
        // Short-circuit here: if neither GPS or Network provider exists don't start listening
        if (Events.GPS_STATUS_CHANGED_EVENT.status == GpsStatusChangedEvent.Status.NONEXISTENT) {
            return;
        }

        // To get a quick first location, query all providers for last known location and treat them
        // like regular fixes by piping them through our normal flow
        final List<String> providers = locationManager.getAllProviders();
        for (String provider : providers) {
            Location location = locationManager.getLastKnownLocation(provider);
            if (location != null) {
                locationListener.onLocationChanged(location);
            }
        }

        registerLocationListeners();
    }

    private void registerLocationListeners() {
        // register existing providers; if one isn't enabled, the listener will take care of that
        requestLocationUpdatesIfProviderExists(LocationManager.GPS_PROVIDER);
        requestLocationUpdatesIfProviderExists(LocationManager.NETWORK_PROVIDER);
    }

    private void requestLocationUpdatesIfProviderExists(String provider) {
        if (locationManager.getProvider(provider) != null) {
            locationManager.requestLocationUpdates(provider,
                    LOCATION_REFRESH_TIME,
                    LOCATION_REFRESH_DISTANCE,
                    locationListener);
        }
    }

    public void handleShutdown() {
        eventService.unregister(this);
        locationManager.removeUpdates(locationListener);
    }

    private void publishNewLocation(Location location) {
        GeoPoint newLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
        ownLocationModel.setLocation(newLocation, location.getAccuracy(), location.getTime());
        eventService.post(Events.NEW_LOCATION_EVENT);
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
