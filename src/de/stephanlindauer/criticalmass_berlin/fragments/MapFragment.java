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
import de.stephanlindauer.criticalmass_berlin.helper.LocationsManager;
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

    private MapView mapView;

    private GeoPoint initialCenter = new GeoPoint((int) (52.520820 * 1E6), (int) (13.409346 * 1E6));
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
        inflater.inflate(R.layout.actionbar_buttons, menu);
    }

    @Override
    public void onActivityCreated(final Bundle savedState) {
        super.onActivityCreated(savedState);

        LocationsManager.getInstance().initialize( getActivity() );

        resourceProxy = new DefaultResourceProxyImpl(getActivity());

        mapView = new MapView( getActivity(), null);
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

        Timer timerRefreshView = new Timer();
        TimerTask timerTaskRefreshView = new TimerTask() {
            @Override
            public void run() {
                try {
                    refreshView();
                }catch (Exception e)
                {
                    //meh...
                }
            }
        };
        timerRefreshView.scheduleAtFixedRate(timerTaskRefreshView, 2000, 10 * 1000);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mapView.getController().animateTo(initialCenter);
            }
        }, 200);
    }

    private void refreshView() {
        for (Overlay element : mapView.getOverlays()) {
            mapView.getOverlays().remove(element);
        }

        if (LocationsManager.getInstance().userLocation != null) {
            GeoPoint currentUserLocation = LocationsManager.getInstance().userLocation;
            ArrayList<OverlayItem> ownOverlay = new ArrayList<OverlayItem>();
            ownOverlay.add(new OverlayItem("", "", currentUserLocation));
            ItemizedIconOverlay userLocationOverlay = new ItemizedIconOverlay<OverlayItem>(ownOverlay, getResources().getDrawable(R.drawable.map_marker_own), null, resourceProxy);

            mapView.getOverlays().add(userLocationOverlay);
        }

        ArrayList<OverlayItem> otherUsersOverlay = new ArrayList<OverlayItem>();

        for (GeoPoint currentOtherUsersLocation : LocationsManager.getInstance().otherUsersLocations) {
            otherUsersOverlay.add(new OverlayItem("", "", currentOtherUsersLocation));
        }
        final ItemizedIconOverlay otherUsersLocationOverlay = new ItemizedIconOverlay<OverlayItem>(otherUsersOverlay, getResources().getDrawable(R.drawable.map_marker), null, resourceProxy);

        mapView.getOverlays().add(otherUsersLocationOverlay);

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mapView.invalidate();
            }
        });
    }
}