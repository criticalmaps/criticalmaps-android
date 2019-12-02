package de.stephanlindauer.criticalmaps.managers;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.squareup.otto.Produce;

import org.osmdroid.util.GeoPoint;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import de.stephanlindauer.criticalmaps.App;
import de.stephanlindauer.criticalmaps.R;
import de.stephanlindauer.criticalmaps.events.Events;
import de.stephanlindauer.criticalmaps.events.GpsStatusChangedEvent;
import de.stephanlindauer.criticalmaps.events.NewLocationEvent;
import de.stephanlindauer.criticalmaps.handler.PermissionCheckHandler;
import de.stephanlindauer.criticalmaps.model.OwnLocationModel;
import de.stephanlindauer.criticalmaps.model.PermissionRequest;
import de.stephanlindauer.criticalmaps.provider.EventBus;

@Singleton
public class LocationUpdateManager {

    private final OwnLocationModel ownLocationModel;
    private final EventBus eventBus;
    private final PermissionCheckHandler permissionCheckHandler;
    private final App app;
    private boolean isUpdating = false;

    //const
    private static final float LOCATION_REFRESH_DISTANCE = 20; //20 meters
    private static final long LOCATION_REFRESH_TIME = 12 * 1000; //12 seconds
    private static final int LOCATION_NEW_THRESHOLD = 30 * 1000; //30 seconds
    private static final String[] USED_PROVIDERS = new String[]{
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER};

    //misc
    private final LocationManager locationManager;
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
                                 EventBus eventBus,
                                 PermissionCheckHandler permissionCheckHandler) {
        this.app = app;
        this.ownLocationModel = ownLocationModel;
        this.eventBus = eventBus;
        this.permissionCheckHandler = permissionCheckHandler;
        locationManager = (LocationManager) app.getSystemService(Context.LOCATION_SERVICE);
    }

    @Produce
    public GpsStatusChangedEvent produceStatusEvent() {
        return Events.GPS_STATUS_CHANGED_EVENT;
    }

    @Produce
    public NewLocationEvent produceLocationEvent() {
        return Events.NEW_LOCATION_EVENT;
    }

    private void postStatusEvent() {
        setStatusEvent();
        eventBus.post(Events.GPS_STATUS_CHANGED_EVENT);
    }

    private void setAndPostPermissionPermanentlyDeniedEvent() {
        Events.GPS_STATUS_CHANGED_EVENT.status =
                GpsStatusChangedEvent.Status.PERMISSION_PERMANENTLY_DENIED;
        eventBus.post(Events.GPS_STATUS_CHANGED_EVENT);
    }

    private void setStatusEvent() {
        // isProviderEnabled() doesn't throw when permission is not granted, so we can use it safely
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Events.GPS_STATUS_CHANGED_EVENT.status = GpsStatusChangedEvent.Status.HIGH_ACCURACY;
            isUpdating = true;
        } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            Events.GPS_STATUS_CHANGED_EVENT.status = GpsStatusChangedEvent.Status.LOW_ACCURACY;
            isUpdating = true;
        } else {
            isUpdating = false;
            Events.GPS_STATUS_CHANGED_EVENT.status = GpsStatusChangedEvent.Status.DISABLED;
        }
    }

    private boolean checkIfAtLeastOneProviderExits() {
        final List<String> allProviders = locationManager.getAllProviders();
        boolean atLeastOneProviderExists = false;
        for (String provider : USED_PROVIDERS) {
            if (allProviders.contains(provider)) {
                atLeastOneProviderExists = true;
            }
        }
        return atLeastOneProviderExists;
    }

    // Usage of this method should rather be handled by listening to the GpsStatusChangedEvent,
    // unfortunately this is not possible in PullServerHandler because we can't register on non
    // main thread
    public boolean isUpdating() {
        return isUpdating;
    }

    public void initializeAndStartListening() {
        boolean noProviderExists = !checkIfAtLeastOneProviderExits();
        boolean noPermission = !checkPermission();

        if (noProviderExists) {
            Events.GPS_STATUS_CHANGED_EVENT.status = GpsStatusChangedEvent.Status.NONEXISTENT;
        } else if (noPermission) {
            Events.GPS_STATUS_CHANGED_EVENT.status = GpsStatusChangedEvent.Status.NO_PERMISSIONS;
        } else {
            setStatusEvent();
        }
        eventBus.register(this);

        // Short-circuit here: if no provider exists don't start listening
        if (noProviderExists) {
            return;
        }

        // If permissions are not granted, don't start listening
        if (noPermission) {
            return;
        }

        startListening();
    }

    private void startListening() {
        // Set GPS status in case we're coming back after permission request
        postStatusEvent();

        // To get a quick first location, query all providers for last known location and treat them
        // like regular fixes by piping them through our normal flow
        final List<String> providers = locationManager.getAllProviders();
        for (String provider : providers) {
            @SuppressLint("MissingPermission")
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

    @SuppressLint("MissingPermission")
    private void requestLocationUpdatesIfProviderExists(String provider) {
        if (locationManager.getProvider(provider) != null) {
            locationManager.requestLocationUpdates(provider,
                    LOCATION_REFRESH_TIME,
                    LOCATION_REFRESH_DISTANCE,
                    locationListener);
        }
    }

    public boolean checkPermission() {
        return PermissionCheckHandler.checkPermissionGranted(
                Manifest.permission.ACCESS_FINE_LOCATION); //TODO does this exist on devices with only network location?
    }

    public void requestPermission() {
        PermissionRequest permissionRequest = new PermissionRequest(
                Manifest.permission.ACCESS_FINE_LOCATION,
                app.getString(R.string.map_location_permissions_rationale_text),
                this::startListening,
                null,
                this::setAndPostPermissionPermanentlyDeniedEvent);
        permissionCheckHandler.requestPermissionWithRationaleIfNeeded(permissionRequest);
    }

    public void handleShutdown() {
        locationManager.removeUpdates(locationListener);
        try {
            eventBus.unregister(this);
        } catch (IllegalArgumentException ignored) {
        }
    }

    private void publishNewLocation(Location location) {
        GeoPoint newLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
        ownLocationModel.setLocation(newLocation, location.getAccuracy());
        eventBus.post(Events.NEW_LOCATION_EVENT);
    }

    private boolean shouldPublishNewLocation(Location location) {
        // Any location is better than no location
        if (lastPublishedLocation == null) {
            return true;
        }

        // Average speed of the CM is ~4 m/s so anything over 30 seconds old, may already
        // be well over 120m off. So a newer fix is assumed to be always better.
        long timeDelta = location.getTime() - lastPublishedLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > LOCATION_NEW_THRESHOLD;
        boolean isSignificantlyOlder = timeDelta < -LOCATION_NEW_THRESHOLD;
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
