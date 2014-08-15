package de.stephanlindauer.criticalmass_berlin.fragments;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.*;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.Toast;
import de.stephanlindauer.criticalmass_berlin.R;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;

public class MapFragment extends Fragment {

    private static final float LOCATION_REFRESH_DISTANCE = 5; //meters
    private static final long LOCATION_REFRESH_TIME = 10000; //milliseconds

    private MapView mapView;
    private ItemizedIconOverlay<OverlayItem> myLocationOverlay;

    private FragmentActivity mContext;

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
        mapView.getOverlays().add( myLocationOverlay );

        mapView.invalidate();

        RelativeLayout RL = (RelativeLayout) getActivity().findViewById(R.id.relativeLayout);
        RL.addView(mapView);

        LocationManager mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME,
                LOCATION_REFRESH_DISTANCE, mLocationListener);

    }
    private final LocationListener mLocationListener = new LocationListener() {

        private ItemizedIconOverlay<OverlayItem> myLocationOverlay1;

        @Override
        public void onLocationChanged(final Location location) {

            GeoPoint currentUserLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
            ArrayList<OverlayItem> ownOverlay = new ArrayList<OverlayItem>();
            ownOverlay.add(new OverlayItem("", "", currentUserLocation));

            DefaultResourceProxyImpl resourceProxy = new DefaultResourceProxyImpl(mContext);

            ItemizedIconOverlay<OverlayItem> oldMyLocationOverlay = myLocationOverlay;
            myLocationOverlay = new ItemizedIconOverlay<OverlayItem>(ownOverlay, getResources().getDrawable(R.drawable.map_marker_own), null, resourceProxy);
            mapView.getOverlays().add(myLocationOverlay);

            mapView.getOverlays().remove( oldMyLocationOverlay );

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