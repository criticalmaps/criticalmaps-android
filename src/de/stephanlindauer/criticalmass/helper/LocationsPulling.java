package de.stephanlindauer.criticalmass.helper;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class LocationsPulling {

    private static final float LOCATION_REFRESH_DISTANCE = 5; //meters
    private static final long LOCATION_REFRESH_TIME = 30000; //milliseconds

    public static final int PULL_OTHER_LOCATIONS_TIME = 30000; //milliseconds

    private static LocationsPulling instance;
    public GeoPoint userLocation = null;
    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            userLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
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
    public List<GeoPoint> otherUsersLocations = new ArrayList<GeoPoint>();
    private FragmentActivity mContext;
    private Timer timerGettingOtherBikers;
    private TimerTask timerTaskGettingsOtherBikers;
    private LocationManager locationManager;
    private boolean initialized = false;
    private String uniqueDeviceIdHashed;

    public static LocationsPulling getInstance() {
        if (LocationsPulling.instance == null) {
            LocationsPulling.instance = new LocationsPulling();
        }
        return LocationsPulling.instance;
    }

    public void initialize(final FragmentActivity mContext) {
        if (initialized == true)
            return;

        this.mContext = mContext;
        this.initialized = true;

        try {
            this.uniqueDeviceIdHashed = AeSimpleSHA1.SHA1( Settings.Secure.getString(mContext.getContentResolver(),
                    Settings.Secure.ANDROID_ID) );
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        //start other bikes location retrieval
        timerGettingOtherBikers = new Timer();
        timerTaskGettingsOtherBikers = new TimerTask() {
            @Override
            public void run() {
                getOtherBikersInfoFromServer();
            }
        };
        timerGettingOtherBikers.scheduleAtFixedRate(timerTaskGettingsOtherBikers, 0, PULL_OTHER_LOCATIONS_TIME);

        //start location tracking
        locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        startLocationListening();
    }

    private void startLocationListening() {
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME,
                LOCATION_REFRESH_DISTANCE, mLocationListener);
    }

    private void stopLocationListening() {
        locationManager.removeUpdates(mLocationListener);
        userLocation = null;
    }

    private void getOtherBikersInfoFromServer() {
        RequestTask request = new RequestTask(uniqueDeviceIdHashed, userLocation, new ICommand() {
            @Override
            public void execute(String... payload) {
                try {
                    JSONObject jsonObject = new JSONObject(payload[0]);
                    Iterator<String> keys = jsonObject.keys();

                    otherUsersLocations = new ArrayList<GeoPoint>();

                    while (keys.hasNext()) {
                        String key = keys.next();
                        JSONObject value = jsonObject.getJSONObject(key);
                        Integer latitude = Integer.parseInt(value.getString("latitude"));
                        Integer longitude = Integer.parseInt(value.getString("longitude"));

                        otherUsersLocations.add(new GeoPoint(latitude, longitude));
                    }
                } catch (Exception e) {
                    return;
                }
            }
        });
        request.execute();
    }

    public void shouldBeTrackingUsersLocation(boolean shouldBeTracking) {
        if (shouldBeTracking)
            startLocationListening();
        else
            stopLocationListening();
    }
}