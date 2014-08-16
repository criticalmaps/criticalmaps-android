package de.stephanlindauer.criticalmass_berlin.fragments;

import android.app.Activity;
import android.content.Context;
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

public class MapFragment extends Fragment {

    private static final float LOCATION_REFRESH_DISTANCE = 5; //meters
    private static final long LOCATION_REFRESH_TIME = 10000; //milliseconds

    private MapView mapView;
    private FragmentActivity mContext;

    private GeoPoint userLocation = null;
    private List<GeoPoint> otherUsersLocations = new ArrayList<GeoPoint>();

    private GeoPoint initialCenter = new GeoPoint((int) (52.520820 * 1E6), (int) (13.409346 * 1E6));
    private Timer timerGettingOtherBikers;
    private TimerTask timerTaskGettingsOtherBikers;
    private DefaultResourceProxyImpl resourceProxy;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.relative_layout, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                System.out.println("fooo");
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public void onCreateOptionsMenu(
            Menu menu, MenuInflater inflater) {
//        inflater.inflate(R.layout.actionbar_buttons, menu);
    }

    @Override
    public void onActivityCreated(final Bundle savedState) {
        super.onActivityCreated(savedState);
        mContext = getActivity();

        resourceProxy = new DefaultResourceProxyImpl(mContext);

        mapView = new MapView(mContext, null);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);
        mapView.getController().setCenter(initialCenter);
        mapView.getController().setZoom(11);
        mapView.setClickable(true);
        mapView.setBuiltInZoomControls(true);
        mapView.setLayoutParams(new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        RelativeLayout RL = (RelativeLayout) getActivity().findViewById(R.id.relativeLayout);
        RL.addView(mapView);

        timerGettingOtherBikers = new Timer();
        timerTaskGettingsOtherBikers = new TimerTask() {
            @Override
            public void run() {
                getOtherBikersInfoFromServer();
            }
        };

        Timer timerRefreshView = new Timer();
        TimerTask timerTaskRefreshView = new TimerTask() {
            @Override
            public void run() {
                refreshView();
            }
        };
        timerRefreshView.scheduleAtFixedRate(timerTaskRefreshView, 2000, 5000);

        getOtherBikersInfoFromServer();

        LocationManager mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME,
                LOCATION_REFRESH_DISTANCE, mLocationListener);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mapView.getController().animateTo(initialCenter);
            }
        }, 200);
    }

    private void refreshView()
    {
        for ( Overlay element : mapView.getOverlays() ) {
            mapView.getOverlays().remove( element );
        }

        if( userLocation != null ) {
            GeoPoint currentUserLocation = userLocation;
            ArrayList<OverlayItem> ownOverlay = new ArrayList<OverlayItem>();
            ownOverlay.add(new OverlayItem("", "", currentUserLocation));
            ItemizedIconOverlay userLocationOverlay = new ItemizedIconOverlay<OverlayItem>(ownOverlay, getResources().getDrawable(R.drawable.map_marker_own), null, resourceProxy);

            mapView.getOverlays().add(userLocationOverlay);
        }

        ArrayList<OverlayItem> otherUsersOverlay = new ArrayList<OverlayItem>();

        for (GeoPoint currentOtherUsersLocation : otherUsersLocations)
        {
            otherUsersOverlay.add(new OverlayItem("", "", currentOtherUsersLocation));
        }
        final ItemizedIconOverlay otherUsersLocationOverlay = new ItemizedIconOverlay<OverlayItem>(otherUsersOverlay, getResources().getDrawable(R.drawable.map_marker), null, resourceProxy);

        mContext.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mapView.getOverlays().add(otherUsersLocationOverlay);
                mapView.invalidate();
            }
        });
    }

    private void getOtherBikersInfoFromServer() {
        String uniqueDeviceId = Settings.Secure.getString(mContext.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        RequestTask request = new RequestTask(uniqueDeviceId, userLocation, new ICommand() {
            @Override
            public void execute(String... payload) {
                try {
                    JSONObject jsonObject = new JSONObject( payload[0] );
                    Iterator<String> keys = jsonObject.keys();

                    otherUsersLocations = new ArrayList<GeoPoint>();

                    while (keys.hasNext()) {
                        String key = keys.next();
                        JSONObject value = jsonObject.getJSONObject(key);
                        Integer latitude =  Integer.parseInt( value.getString("latitude"));
                        Integer longitude = Integer.parseInt( value.getString("longitude"));

                        otherUsersLocations.add( new GeoPoint( latitude, longitude ) );
                    }

                    timerGettingOtherBikers.schedule(timerTaskGettingsOtherBikers, 30 * 1000);
                } catch (Exception e) {
                    return;
                }
            }
        });

        request.execute();
    }

    public final LocationListener mLocationListener = new LocationListener() {
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
}