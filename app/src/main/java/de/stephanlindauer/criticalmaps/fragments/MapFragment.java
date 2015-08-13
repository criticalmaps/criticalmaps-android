package de.stephanlindauer.criticalmaps.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.squareup.otto.Subscribe;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;

import de.stephanlindauer.criticalmaps.R;
import de.stephanlindauer.criticalmaps.events.NewLocationEvent;
import de.stephanlindauer.criticalmaps.events.NewOverlayConfigEvent;
import de.stephanlindauer.criticalmaps.events.NewServerResponseEvent;
import de.stephanlindauer.criticalmaps.model.OtherUsersLocationModel;
import de.stephanlindauer.criticalmaps.model.OwnLocationModel;
import de.stephanlindauer.criticalmaps.model.SternfahrtModel;
import de.stephanlindauer.criticalmaps.service.EventService;
import de.stephanlindauer.criticalmaps.service.GPSMananger;
import de.stephanlindauer.criticalmaps.utils.MapViewUtils;

public class MapFragment extends SuperFragment {

    //dependencies
    private OwnLocationModel ownLocationModel = OwnLocationModel.getInstance();
    private OtherUsersLocationModel otherUsersLocationModel = OtherUsersLocationModel.getInstance();
    private EventService eventService = EventService.getInstance();
    private SternfahrtModel sternfahrtModel = SternfahrtModel.getInstance();
    private GPSMananger locationManager = GPSMananger.getInstance();

    //view
    private MapView mapView;
    private ImageButton setCurrentLocationCenter;
    private RelativeLayout mapContainer;
    private RelativeLayout searchingForLocationOverlay;

    //misc
    private DefaultResourceProxyImpl resourceProxy;
    private boolean isInitialLocationSet = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.map, container, false);
    }

    @Override
    public void onActivityCreated(final Bundle savedState) {
        super.onActivityCreated(savedState);

        resourceProxy = new DefaultResourceProxyImpl(getActivity());

        noTrackingOverlay = (Button) getActivity().findViewById(R.id.noTrackingOverlay);
        setCurrentLocationCenter = (ImageButton) getActivity().findViewById(R.id.setCurrentLocationCenter);
        mapContainer = (RelativeLayout) getActivity().findViewById(R.id.mapContainer);
        searchingForLocationOverlay = (RelativeLayout) getActivity().findViewById(R.id.searchingForLocationOverlay);

        mapView = MapViewUtils.createMapView(getActivity());
        mapContainer.addView(mapView);
        mapView.invalidate();

        setLastKnownLocationBoundingBox();
        setLastKnownLocationMapIcon();

        noTrackingOverlay.setVisibility(ownLocationModel.isListeningForLocation ? View.INVISIBLE : View.VISIBLE);
        noTrackingOverlay.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                noTrackingOverlay.setVisibility(View.INVISIBLE);
                GPSMananger.getInstance().setTrackingUserLocation(true);
                trackingToggleButton.setChecked(true);
            }
        });

        setCurrentLocationCenter.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (ownLocationModel.ownLocation != null)
                    mapView.getController().animateTo(ownLocationModel.ownLocation);
            }
        });
    }

    private void setLastKnownLocationBoundingBox() {
        final GeoPoint lastKnownLocation = locationManager.getLastKnownLocation();
        if (lastKnownLocation != null) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mapView.getController().animateTo(lastKnownLocation);
                }
            }, 200);
        }
    }

    private void setLastKnownLocationMapIcon() {
        final GeoPoint ownLocation = ownLocationModel.ownLocation;
        if (ownLocation != null) {
            isInitialLocationSet = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mapView.getController().animateTo(ownLocation);
                }
            }, 200);
        } else {
            searchingForLocationOverlay.setVisibility(View.VISIBLE);
        }

    }

    private void refreshView() {
        for (Overlay element : mapView.getOverlays()) {
            if (element instanceof Polyline)
                continue;//don't delete polylines

            mapView.getOverlays().remove(element);
        }

        if (ownLocationModel.ownLocation != null) {
            GeoPoint currentUserLocation = ownLocationModel.ownLocation;
            ArrayList<OverlayItem> ownOverlay = new ArrayList<OverlayItem>();
            ownOverlay.add(new OverlayItem("", "", currentUserLocation));
            ItemizedIconOverlay userLocationOverlay = new ItemizedIconOverlay<OverlayItem>(ownOverlay, getResources().getDrawable(R.drawable.map_marker_own), null, resourceProxy);

            mapView.getOverlays().add(userLocationOverlay);
        }

        ArrayList<OverlayItem> otherUsersOverlay = new ArrayList<OverlayItem>();

        for (GeoPoint currentOtherUsersLocation : otherUsersLocationModel.getOtherUsersLocations()) {
            otherUsersOverlay.add(new OverlayItem("", "", currentOtherUsersLocation));
        }
        final ItemizedIconOverlay otherUsersLocationOverlay = new ItemizedIconOverlay<OverlayItem>(otherUsersOverlay, getResources().getDrawable(R.drawable.map_marker), null, resourceProxy);

        mapView.getOverlays().add(otherUsersLocationOverlay);

        if (shouldShowSternfahrtRoutes) {
            ArrayList<Polyline> sternfahrtOverlays = sternfahrtModel.getAllOverlays(getActivity());
            for (Polyline route : sternfahrtOverlays) {
                mapView.getOverlays().add(route);
            }
        } else {
            for (Overlay element : mapView.getOverlays()) {
                if (element instanceof Polyline)
                    mapView.getOverlays().remove(element);
            }
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mapView.invalidate();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshView();
        eventService.register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        eventService.unregister(this);
    }

    @Subscribe
    public void handleNewServerData(NewServerResponseEvent e) {
        refreshView();
    }

    @Subscribe
    public void handleNewLocation(NewLocationEvent e) {
        if (!isInitialLocationSet) {
            hideSearchingForLocationOverlayAndZoomToLocation();
            isInitialLocationSet = true;
        }
        refreshView();
    }

    private void hideSearchingForLocationOverlayAndZoomToLocation() {
        searchingForLocationOverlay.setVisibility(View.GONE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mapView.getController().animateTo(ownLocationModel.ownLocation);
            }
        }, 200);
    }

    @Subscribe
    public void handleNewOverlayConfig(NewOverlayConfigEvent e) {
        refreshView();
    }
}