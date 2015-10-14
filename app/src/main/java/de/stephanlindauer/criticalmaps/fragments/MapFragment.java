package de.stephanlindauer.criticalmaps.fragments;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.squareup.otto.Subscribe;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.BindDrawable;
import butterknife.ButterKnife;
import de.stephanlindauer.criticalmaps.R;
import de.stephanlindauer.criticalmaps.events.NewLocationEvent;
import de.stephanlindauer.criticalmaps.events.NewOverlayConfigEvent;
import de.stephanlindauer.criticalmaps.events.NewServerResponseEvent;
import de.stephanlindauer.criticalmaps.model.OtherUsersLocationModel;
import de.stephanlindauer.criticalmaps.model.OwnLocationModel;
import de.stephanlindauer.criticalmaps.model.SternfahrtModel;
import de.stephanlindauer.criticalmaps.overlays.LocationMarker;
import de.stephanlindauer.criticalmaps.provider.EventBusProvider;
import de.stephanlindauer.criticalmaps.service.LocationUpdatesService;
import de.stephanlindauer.criticalmaps.utils.MapViewUtils;

public class MapFragment extends Fragment {

    //dependencies
    private OwnLocationModel ownLocationModel = OwnLocationModel.getInstance();
    private OtherUsersLocationModel otherUsersLocationModel = OtherUsersLocationModel.getInstance();
    private EventBusProvider eventService = EventBusProvider.getInstance();
    private SternfahrtModel sternfahrtModel = SternfahrtModel.getInstance();
    private LocationUpdatesService locationManager = LocationUpdatesService.getInstance();

    //view
    private MapView mapView;

    @Bind(R.id.set_current_location_center)
    ImageButton setCurrentLocationCenter;

    @Bind(R.id.map_container)
    RelativeLayout mapContainer;

    @Bind(R.id.searching_for_location_overlay_map)
    RelativeLayout searchingForLocationOverlay;

    //misc
    private DefaultResourceProxyImpl resourceProxy;
    private boolean isInitialLocationSet = false;

    //cache drawables
    @BindDrawable(R.drawable.map_marker)
    Drawable locationIcon;

    @BindDrawable(R.drawable.map_marker_own)
    Drawable ownLocationIcon;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(final Bundle savedState) {
        super.onActivityCreated(savedState);

        resourceProxy = new DefaultResourceProxyImpl(getActivity().getApplication());

        mapView = MapViewUtils.createMapView(getActivity(), resourceProxy);
        mapContainer.addView(mapView);

        setCurrentLocationCenter.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (ownLocationModel.ownLocation != null)
                    animateToLocation(ownLocationModel.ownLocation);
            }
        });

        setMapAndOverlayState();
    }

    private void setMapAndOverlayState() {
        // fresh start or recreated before first location was found
        if (ownLocationModel.ownLocation == null) {
            searchingForLocationOverlay.setVisibility(View.VISIBLE);

            GeoPoint lastKnownLocation = locationManager.getLastKnownLocation();
            if (lastKnownLocation != null) {
                setToLocation(lastKnownLocation);
            }
        } else {
            // if first location was already handled before just set current location
            if (isInitialLocationSet) {
                setToLocation(ownLocationModel.ownLocation);
            }
        }
    }

    private void refreshView() {
        mapView.getOverlays().clear();

        if (sternfahrtModel.shouldShowSternfahrtRoutes) {
            ArrayList<Polyline> sternfahrtOverlays = sternfahrtModel.getAllOverlays(getActivity().getApplication());
            for (Polyline route : sternfahrtOverlays) {
                mapView.getOverlays().add(route);
            }
        }

        for (GeoPoint currentOtherUsersLocation : otherUsersLocationModel.getOtherUsersLocations()) {
            LocationMarker otherPeoplesMarker = new LocationMarker(mapView, resourceProxy);
            otherPeoplesMarker.setPosition(currentOtherUsersLocation);
            otherPeoplesMarker.setIcon(locationIcon);
            mapView.getOverlays().add(otherPeoplesMarker);
        }

        if (ownLocationModel.ownLocation != null) {
            GeoPoint currentUserLocation = ownLocationModel.ownLocation;
            LocationMarker ownMarker = new LocationMarker(mapView, resourceProxy);
            ownMarker.setPosition(currentUserLocation);
            ownMarker.setIcon(ownLocationIcon);
            mapView.getOverlays().add(ownMarker);
        }

        mapView.invalidate();
    }

    @Override
    public void onResume() {
        super.onResume();

        // we got the first location while the app was in background
        if (ownLocationModel.ownLocation != null && !isInitialLocationSet){
            handleFirstLocationUpdate();
        }

        eventService.register(this);

        refreshView();
    }

    private void handleFirstLocationUpdate() {
        searchingForLocationOverlay.setVisibility(View.GONE);
        animateToLocation(ownLocationModel.ownLocation);
        isInitialLocationSet = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        eventService.unregister(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mapView = null;
        ButterKnife.unbind(this);
    }

    @Subscribe
    public void handleNewServerData(NewServerResponseEvent e) {
        refreshView();
    }

    @Subscribe
    public void handleNewLocation(NewLocationEvent e) {
        // if this is the first location update handle it accordingly
        if (!isInitialLocationSet) {
            handleFirstLocationUpdate();
        }

        refreshView();
    }

    @Subscribe
    public void handleNewOverlayConfig(NewOverlayConfigEvent e) {
        refreshView();
    }

    private void animateToLocation(final GeoPoint location) {
        mapView.getController().animateTo(location);
    }

    private void setToLocation(GeoPoint lastKnownLocation) {
        mapView.getController().setCenter(lastKnownLocation);
    }
}