package de.stephanlindauer.criticalmass_berlin.helper;

import org.osmdroid.util.GeoPoint;import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.*;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;
import de.stephanlindauer.criticalmass_berlin.R;
import de.stephanlindauer.criticalmass_berlin.helper.ICommand;
import de.stephanlindauer.criticalmass_berlin.helper.RequestTask;
import org.json.JSONObject;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.*;

public class LocationsManager {

    private static final float LOCATION_REFRESH_DISTANCE = 5; //meters
    private static final long LOCATION_REFRESH_TIME = 10000; //milliseconds

    public GeoPoint userLocation = null;
    public List<GeoPoint> otherUsersLocations = new ArrayList<GeoPoint>();

    private boolean initialized = false;

    private LocationsManager() {}

    private FragmentActivity mContext;

    private Timer timerGettingOtherBikers;
    private TimerTask timerTaskGettingsOtherBikers;

    private static LocationsManager instance;


    public static LocationsManager getInstance () {
        if (LocationsManager.instance == null) {
            LocationsManager.instance = new LocationsManager();
        }
        return LocationsManager.instance;
    }

    public void initialize( FragmentActivity mContext )
    {
        if(initialized == true)
            return;

        this.mContext = mContext;
        this.initialized = true;

        //start other bikes location retrieval
        timerGettingOtherBikers = new Timer();
        timerTaskGettingsOtherBikers = new TimerTask() {
            @Override
            public void run() {
                getOtherBikersInfoFromServer();
            }
        };
        timerGettingOtherBikers.scheduleAtFixedRate(timerTaskGettingsOtherBikers, 0, 2000);

        //start location tracking
        LocationManager mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME,
                LOCATION_REFRESH_DISTANCE, mLocationListener);
    }


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

    private void getOtherBikersInfoFromServer() {
        String uniqueDeviceId = Settings.Secure.getString(mContext.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        RequestTask request = new RequestTask(uniqueDeviceId, userLocation, new ICommand() {
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
}