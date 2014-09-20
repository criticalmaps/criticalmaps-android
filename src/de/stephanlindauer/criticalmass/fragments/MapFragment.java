package de.stephanlindauer.criticalmass.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.RelativeLayout;
import de.stephanlindauer.criticalmass.R;
import de.stephanlindauer.criticalmass.helper.LocationsPulling;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MapFragment extends SuperFragment {

    private MapView mapView;

    private GeoPoint initialCenter = new GeoPoint((int) (52.520820 * 1E6), (int) (13.409346 * 1E6));
    private DefaultResourceProxyImpl resourceProxy;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.map, container, false);
    }

    @Override
    public void onActivityCreated(final Bundle savedState) {
        super.onActivityCreated(savedState);

        LocationsPulling.getInstance().initialize(getActivity());

        resourceProxy = new DefaultResourceProxyImpl(getActivity());

        mapView = new MapView(getActivity(), null);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);
        mapView.getController().setCenter(initialCenter);
        mapView.getController().setZoom(12);
        mapView.setClickable(true);
        mapView.setBuiltInZoomControls(true);
        mapView.setLayoutParams(new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        RelativeLayout mapCountainer = (RelativeLayout) getActivity().findViewById(R.id.mapContainer);
        mapCountainer.addView(mapView);

        noTrackingOverlay = (Button) getActivity().findViewById(R.id.noTrackingOverlay);
        noTrackingOverlay.setVisibility(LocationsPulling.getInstance().isListeningForLocation() ? View.INVISIBLE : View.VISIBLE);
        noTrackingOverlay.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                noTrackingOverlay.setVisibility(View.INVISIBLE);
                LocationsPulling.getInstance().shouldBeTrackingUsersLocation(true);
                trackingToggleButton.setChecked(true);
            }
        });

        Timer timerRefreshView = new Timer();
        TimerTask timerTaskRefreshView = new TimerTask() {
            @Override
            public void run() {
                try {
                    refreshView();
                } catch (Exception e) {
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

        if (LocationsPulling.getInstance().userLocation != null) {
            GeoPoint currentUserLocation = LocationsPulling.getInstance().userLocation;
            ArrayList<OverlayItem> ownOverlay = new ArrayList<OverlayItem>();
            ownOverlay.add(new OverlayItem("", "", currentUserLocation));
            ItemizedIconOverlay userLocationOverlay = new ItemizedIconOverlay<OverlayItem>(ownOverlay, getResources().getDrawable(R.drawable.map_marker_own), null, resourceProxy);

            mapView.getOverlays().add(userLocationOverlay);
        }

        ArrayList<OverlayItem> otherUsersOverlay = new ArrayList<OverlayItem>();

        for (GeoPoint currentOtherUsersLocation : LocationsPulling.getInstance().otherUsersLocations) {
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