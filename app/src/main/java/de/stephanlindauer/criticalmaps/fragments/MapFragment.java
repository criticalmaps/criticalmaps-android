package de.stephanlindauer.criticalmaps.fragments;

import android.animation.AnimatorInflater;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorUpdateListener;
import android.support.v7.app.AlertDialog;
import android.support.v7.content.res.AppCompatResources;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.squareup.otto.Subscribe;

import de.stephanlindauer.criticalmaps.App;
import de.stephanlindauer.criticalmaps.R;
import de.stephanlindauer.criticalmaps.events.GpsStatusChangedEvent;
import de.stephanlindauer.criticalmaps.events.NetworkConnectivityChangedEvent;
import de.stephanlindauer.criticalmaps.events.NewLocationEvent;
import de.stephanlindauer.criticalmaps.events.NewServerResponseEvent;
import de.stephanlindauer.criticalmaps.managers.LocationUpdateManager;
import de.stephanlindauer.criticalmaps.model.OtherUsersLocationModel;
import de.stephanlindauer.criticalmaps.model.OwnLocationModel;
import de.stephanlindauer.criticalmaps.overlays.LocationMarker;
import de.stephanlindauer.criticalmaps.provider.EventBus;
import de.stephanlindauer.criticalmaps.utils.AlertBuilder;
import de.stephanlindauer.criticalmaps.utils.MapViewUtils;

import javax.inject.Inject;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;

public class MapFragment extends Fragment {

    // constants
    private final static String KEY_MAP_ZOOMLEVEL = "map_zoomlevel";
    private final static String KEY_MAP_POSITION = "map_position";
    private final static String KEY_MAP_ORIENTATION = "map_orientation";
    private final static String KEY_INITIAL_LOCATION_SET = "initial_location_set";

    //dependencies
    @Inject
    OwnLocationModel ownLocationModel;

    @Inject
    OtherUsersLocationModel otherUsersLocationModel;

    @Inject
    EventBus eventBus;

    @Inject
    LocationUpdateManager locationUpdateManager;

    //view
    private MapView mapView;

    @BindView(R.id.set_current_location_center)
    FloatingActionButton setCurrentLocationCenter;

    @BindView(R.id.set_rotation_north)
    FloatingActionButton setRotationNorth;

    @BindView(R.id.map_container)
    RelativeLayout mapContainer;

    @BindView(R.id.map_osm_notice)
    TextView osmNoticeOverlay;

    @BindView((R.id.map_no_data_connectivity))
    FloatingActionButton noDataConnectivityButton;

    //misc
    private boolean isInitialLocationSet = false;
    private boolean mightComeBackWithLocationPermission = false;
    private ObjectAnimator gpsSearchingAnimator;

    //cache drawables
    private Drawable locationIcon;
    private Drawable ownLocationIcon;

    private Unbinder unbinder;

    //OnClickListeners for location FAB
    private final View.OnClickListener centerLocationOnClickListener =  new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (ownLocationModel.ownLocation != null)
                animateToLocation(ownLocationModel.ownLocation);
        }
    };

    //OnClickListeners for rotate north FAB
    private final View.OnClickListener rotationNorthOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            float currentRotation = mapView.getMapOrientation() % 360;
            if (currentRotation < 0.0f) {
                currentRotation = 360.0f + currentRotation;
                setRotationNorth.setRotation(currentRotation);
                mapView.setMapOrientation(currentRotation);
            }

            float destinationRotation = currentRotation > 180.0f ? 360.0f : 0.0f;
            ViewCompat.animate(setRotationNorth)
                    .rotation(destinationRotation)
                    .setDuration(300L)
                    .setUpdateListener(new ViewPropertyAnimatorUpdateListener(){
                        @Override
                        public  void onAnimationUpdate(View view){
                            mapView.setMapOrientation(view.getRotation());
                        }
                    })
                    .start();
        }
    };

    private final View.OnClickListener noGpsOnClickListener =  new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AlertBuilder.show(getActivity(),
                    R.string.map_no_gps_title,
                    R.string.map_no_gps_text);
        }
    };

    private final View.OnClickListener GpsDisabledOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AlertBuilder.show(getActivity(),
                    R.string.map_gps_disabled_title,
                    R.string.map_gps_disabled_text);
        }
    };

    private final View.OnClickListener GpsNoPermissionsOnClickListener =  new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            locationUpdateManager.requestPermission();
        }
    };

    private final View.OnClickListener GpsPermissionsPermanentlyDeniedOnClickListener =  new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            new AlertDialog.Builder(getActivity(), R.style.AlertDialogTheme)
                    .setTitle(R.string.map_gps_permissions_permanently_denied_title)
                    .setMessage(R.string.map_gps_permissions_permanently_denied_text)
                    .setNegativeButton(R.string.no, null)
                    .setPositiveButton(R.string.permissions_open_settings, (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", getActivity().getPackageName(), null));
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);})
                    .create()
                    .show();
        }
    };

    private final View.OnClickListener searchingForLocationOnClickListener =  new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Toast.makeText(getActivity(), R.string.map_searching_for_location, Toast.LENGTH_SHORT).show();
        }
    };


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_map, container, false);
        unbinder = ButterKnife.bind(this, view);

        //noinspection ConstantConditions
        locationIcon = AppCompatResources.getDrawable(getActivity(), R.drawable.ic_map_marker);
        ownLocationIcon = AppCompatResources.getDrawable(getActivity(), R.drawable.ic_map_marker_own);

        return view;
    }

    @Override
    public void onActivityCreated(final Bundle savedState) {
        super.onActivityCreated(savedState);

        App.components().inject(this);

        osmNoticeOverlay.setMovementMethod(LinkMovementMethod.getInstance());

        mapView = MapViewUtils.createMapView(getActivity());
        mapContainer.addView(mapView);

        setCurrentLocationCenter.setOnClickListener(centerLocationOnClickListener);
        setRotationNorth.setOnClickListener(rotationNorthOnClickListener);

        noDataConnectivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertBuilder.show(getActivity(),
                        R.string.map_no_internet_connection_title,
                        R.string.map_no_internet_connection_text);
            }
        });

        RotationGestureOverlay mRotationGestureOverlay = new RotationGestureOverlay(mapView) {
            @Override
            public boolean onTouchEvent(MotionEvent event, MapView mapView) {
                setRotationNorth.setRotation(mapView.getMapOrientation());
                return super.onTouchEvent(event, mapView);
            }
        };
        mRotationGestureOverlay.setEnabled(true);
        mapView.setMultiTouchControls(true);
        mapView.getOverlays().add(mRotationGestureOverlay);

        if (savedState != null) {
            Integer zoomLevel = (Integer) savedState.get(KEY_MAP_ZOOMLEVEL);
            GeoPoint position = savedState.getParcelable(KEY_MAP_POSITION);
            Float orientation = (Float) savedState.get(KEY_MAP_ORIENTATION);

            if (zoomLevel != null && position != null && orientation != null) {
                mapView.getController().setZoom(zoomLevel);
                mapView.setMapOrientation(orientation);
                setToLocation(position);
            }

            isInitialLocationSet = savedState.getBoolean(KEY_INITIAL_LOCATION_SET, false);
        }
        setRotationNorth.setRotation(mapView.getMapOrientation());
    }

    private void refreshView() {
        for (Overlay overlay : mapView.getOverlays()) {
            if (overlay instanceof LocationMarker) {
                mapView.getOverlays().remove(overlay);
            }
        }

        for (GeoPoint currentOtherUsersLocation : otherUsersLocationModel.getOtherUsersLocations()) {
            LocationMarker otherPeoplesMarker = new LocationMarker(mapView);
            otherPeoplesMarker.setPosition(currentOtherUsersLocation);
            otherPeoplesMarker.setIcon(locationIcon);
            mapView.getOverlays().add(otherPeoplesMarker);
        }

        if (ownLocationModel.ownLocation != null) {
            GeoPoint currentUserLocation = ownLocationModel.ownLocation;
            LocationMarker ownMarker = new LocationMarker(mapView);
            ownMarker.setPosition(currentUserLocation);
            ownMarker.setIcon(ownLocationIcon);
            mapView.getOverlays().add(ownMarker);
        }

        mapView.invalidate();
    }

    @Override
    public void onResume() {
        super.onResume();
        eventBus.register(this);

        // Workaround to handle case when location permission was granted via app ops while the
        // app is running.
        if (mightComeBackWithLocationPermission) {
            // additional check needed because requesting permission will always trigger onResume()
            // even if no dialog is shown. This would send us into an infinite loop
            if (locationUpdateManager.checkPermission()) {
                locationUpdateManager.requestPermission();
            }
        }
    }

    private void handleFirstLocationUpdate() {
        setGpsStatusFixed();
        zoomToLocation(ownLocationModel.ownLocation, 12);
        isInitialLocationSet = true;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(KEY_MAP_ZOOMLEVEL, mapView.getZoomLevel());
        outState.putParcelable(KEY_MAP_POSITION, (GeoPoint) mapView.getMapCenter());
        outState.putFloat(KEY_MAP_ORIENTATION, mapView.getMapOrientation());
        outState.putBoolean(KEY_INITIAL_LOCATION_SET, isInitialLocationSet);
    }

    @Override
    public void onPause() {
        super.onPause();
        eventBus.unregister(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mapView = null;
        unbinder.unbind();
    }

    @Subscribe
    public void handleNewServerData(NewServerResponseEvent e) {
        refreshView();
    }

    @Subscribe
    public void handleNewLocation(NewLocationEvent e) {
        // if this is the first location update handle it accordingly
        if (ownLocationModel.ownLocation != null && !isInitialLocationSet) {
            handleFirstLocationUpdate();
        }

        refreshView();
    }

    @Subscribe
    public void handleNetworkConnectivityChanged(NetworkConnectivityChangedEvent e) {
        noDataConnectivityButton.setVisibility(e.isConnected ? View.GONE : View.VISIBLE);
    }

    @Subscribe
    public void handleGpsStatusChangedEvent(GpsStatusChangedEvent e) {
        mightComeBackWithLocationPermission = false;
        if (e.status == GpsStatusChangedEvent.Status.NONEXISTENT) {
            setGpsStatusNonexistent();
        } else if (e.status == GpsStatusChangedEvent.Status.DISABLED) {
            setGpsStatusDisabled();
        } else if (e.status == GpsStatusChangedEvent.Status.PERMISSION_PERMANENTLY_DENIED) {
            mightComeBackWithLocationPermission = true;
            setGpsStatusPermissionsPermanentlyDenied();
        } else if (e.status == GpsStatusChangedEvent.Status.NO_PERMISSIONS) {
            mightComeBackWithLocationPermission = true;
            setGpsStatusNoPermissions();
        } else if (e.status == GpsStatusChangedEvent.Status.LOW_ACCURACY ||
                e.status == GpsStatusChangedEvent.Status.HIGH_ACCURACY) {
            if (ownLocationModel.ownLocation != null) {
                setGpsStatusFixed();
            } else {
                setGpsStatusSearching();
            }
        }
    }

    private void setGpsStatusNonexistent() {
        cancelGpsSearchingAnimationIfRunning();
        setGpsStatusCommon(R.color.map_fab_warning, R.drawable.ic_gps_off_white_24dp,
                noGpsOnClickListener);
    }

    private void setGpsStatusDisabled() {
        cancelGpsSearchingAnimationIfRunning();
        setGpsStatusCommon(R.color.map_fab_warning, R.drawable.ic_gps_off_white_24dp,
                GpsDisabledOnClickListener);
    }

    private void setGpsStatusNoPermissions() {
        cancelGpsSearchingAnimationIfRunning();
        setGpsStatusCommon(R.color.map_fab_warning, R.drawable.ic_gps_off_white_24dp,
                GpsNoPermissionsOnClickListener); // TODO Text/listener, Permission Icon?
    }

    private void setGpsStatusPermissionsPermanentlyDenied() {
        cancelGpsSearchingAnimationIfRunning();
        setGpsStatusCommon(R.color.map_fab_warning, R.drawable.ic_gps_off_white_24dp,
                GpsPermissionsPermanentlyDeniedOnClickListener); // TODO Text/listener, Permission Icon?
    }

    private void setGpsStatusFixed() {
        cancelGpsSearchingAnimationIfRunning();
        setGpsStatusCommon(R.color.colorAccent, R.drawable.ic_gps_fixed_white_24dp,
                centerLocationOnClickListener);
    }

    private void setGpsStatusSearching() {
        cancelGpsSearchingAnimationIfRunning();
        setGpsStatusCommon(R.color.map_fab_searching, R.drawable.ic_gps_not_fixed_white_24dp,
                searchingForLocationOnClickListener);

        gpsSearchingAnimator = (ObjectAnimator) AnimatorInflater.loadAnimator(
                getActivity(),
                R.animator.map_gps_fab_searching_animation);
        gpsSearchingAnimator.setTarget(setCurrentLocationCenter);
        gpsSearchingAnimator.start();
    }

    private void setGpsStatusCommon(@ColorRes int colorResId, @DrawableRes int iconResId,
                                    View.OnClickListener onClickListener) {
        //noinspection ConstantConditions
        setCurrentLocationCenter.setBackgroundTintList(
                ContextCompat.getColorStateList(getActivity(), colorResId));
        setCurrentLocationCenter.setImageResource(iconResId);
        setCurrentLocationCenter.setOnClickListener(onClickListener);
    }

    private void cancelGpsSearchingAnimationIfRunning() {
        if (gpsSearchingAnimator != null) {
            gpsSearchingAnimator.cancel();
            setCurrentLocationCenter.setAlpha(1.0f);
        }
    }

    private void zoomToLocation(final GeoPoint location, final int zoomLevel) {
        // TODO use setCenter() + zoomTo() here; currently broken and ends up in a wrong location
        mapView.getController().setZoom(zoomLevel);
        animateToLocation(location);
    }

    private void animateToLocation(final GeoPoint location) {
        mapView.getController().animateTo(location);
    }

    private void setToLocation(final GeoPoint location) {
        mapView.getController().setCenter(location);
    }
}
