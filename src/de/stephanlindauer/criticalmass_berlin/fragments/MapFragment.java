package de.stephanlindauer.criticalmass_berlin.fragments;

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

import java.util.ArrayList;
import java.util.Iterator;

public class MapFragment extends Fragment {

    private static final float LOCATION_REFRESH_DISTANCE = 5; //meters
    private static final long LOCATION_REFRESH_TIME = 10000; //milliseconds

    private MapView mapView;
    private ItemizedIconOverlay<OverlayItem> myLocationOverlay;

    private FragmentActivity mContext;
    public Location currentLocation;


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
            // action with ID action_settings was selected
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
        inflater.inflate(R.layout.actionbar_buttons, menu);
    }

    @Override
    public void onActivityCreated(final Bundle savedState) {
        super.onActivityCreated(savedState);
        mContext = getActivity();

        mapView = new MapView(mContext, null);

        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);

        GeoPoint point = new GeoPoint((int) (52.520820 * 1E6), (int) (13.409346 * 1E6));
        mapView.getController().setCenter(point);
        mapView.getController().setZoom(11);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                GeoPoint point = new GeoPoint((int) (52.520820 * 1E6), (int) (13.409346 * 1E6));
                mapView.getController().animateTo(point);
            }
        }, 200);

        mapView.setClickable(true);
        mapView.setBuiltInZoomControls(true);
        mapView.setLayoutParams(new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        ArrayList<OverlayItem> overlays = new ArrayList<OverlayItem>();

        overlays.add(new OverlayItem("New Overlay", "Overlay Description", new GeoPoint((int) (52.468190 * 1E6), (int) (13.426046 * 1E6))));

        DefaultResourceProxyImpl resourceProxy = new DefaultResourceProxyImpl(mContext);
        ItemizedIconOverlay<OverlayItem> myLocationOverlay = new ItemizedIconOverlay<OverlayItem>(overlays, getResources().getDrawable(R.drawable.map_marker), null, resourceProxy);
        mapView.getOverlays().add(myLocationOverlay);

        mapView.invalidate();

        startHttpPulling();

        RelativeLayout RL = (RelativeLayout) getActivity().findViewById(R.id.relativeLayout);
        RL.addView(mapView);

        LocationManager mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME,
                LOCATION_REFRESH_DISTANCE, mLocationListener);
    }

    private void startHttpPulling() {
        String uniqueDeviceId = Settings.Secure.getString(mContext.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        RequestTask request = new RequestTask(uniqueDeviceId, currentLocation, new ICommand() {
            @Override
            public void execute(String... payload) {
                try {
                    JSONObject jsonObject = null;
                    jsonObject = new JSONObject((String) payload[0]);

                    Iterator<String> keys = jsonObject.keys();

                    ArrayList<OverlayItem> otherCyclistsOverlay = new ArrayList<OverlayItem>();
                    while (keys.hasNext()) {
                        String key = keys.next();

                        JSONObject value = jsonObject.getJSONObject(key);
                        String timestamp = value.getString("timestamp");
                        double longitude = Double.parseDouble(value.getString("longitude")) * 1E6;
                        double latitude = Double.parseDouble(value.getString("latitude")) * 1E6;

                        otherCyclistsOverlay.add(new OverlayItem(key, timestamp, new GeoPoint( latitude, longitude )));
                    }

                    for ( Overlay element : mapView.getOverlays() ) {
                        if( element != myLocationOverlay )
                        {
                            mapView.getOverlays().remove( element );
                        }
                    }

                    DefaultResourceProxyImpl resourceProxy = new DefaultResourceProxyImpl( mContext );
                    ItemizedIconOverlay otherCyclistsLocationOverlay = new ItemizedIconOverlay<OverlayItem>(otherCyclistsOverlay, getResources().getDrawable(R.drawable.map_marker), null, resourceProxy);

                    mapView.getOverlays().add(otherCyclistsLocationOverlay);
                    mapView.invalidate();

                } catch (Exception e) {
                    return;
                }
            }
        });


        request.execute();
    }

    public final LocationListener mLocationListener = new LocationListener() {

        private ItemizedIconOverlay<OverlayItem> myLocationOverlay1;

        @Override
        public void onLocationChanged(final Location location) {
            currentLocation = location;

            GeoPoint currentUserLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
            ArrayList<OverlayItem> ownOverlay = new ArrayList<OverlayItem>();
            ownOverlay.add(new OverlayItem("", "", currentUserLocation));

            DefaultResourceProxyImpl resourceProxy = new DefaultResourceProxyImpl(mContext);

            ItemizedIconOverlay<OverlayItem> oldMyLocationOverlay = myLocationOverlay;
            myLocationOverlay = new ItemizedIconOverlay<OverlayItem>(ownOverlay, getResources().getDrawable(R.drawable.map_marker_own), null, resourceProxy);

            mapView.getOverlays().add(myLocationOverlay);
            mapView.getOverlays().remove(oldMyLocationOverlay);

            mapView.invalidate();
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