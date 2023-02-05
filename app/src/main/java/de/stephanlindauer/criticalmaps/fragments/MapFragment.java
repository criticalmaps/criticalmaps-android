package de.stephanlindauer.criticalmaps.fragments;

import android.animation.AnimatorInflater;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;

import com.squareup.otto.Subscribe;

import org.osmdroid.tileprovider.modules.SqlTileWriter;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.infowindow.InfoWindow;

import javax.inject.Inject;

import de.stephanlindauer.criticalmaps.App;
import de.stephanlindauer.criticalmaps.R;
import de.stephanlindauer.criticalmaps.databinding.FragmentMapBinding;
import de.stephanlindauer.criticalmaps.events.GpsStatusChangedEvent;
import de.stephanlindauer.criticalmaps.events.NetworkConnectivityChangedEvent;
import de.stephanlindauer.criticalmaps.events.NewLocationEvent;
import de.stephanlindauer.criticalmaps.events.NewServerResponseEvent;
import de.stephanlindauer.criticalmaps.handler.ShowGpxHandler;
import de.stephanlindauer.criticalmaps.managers.LocationUpdateManager;
import de.stephanlindauer.criticalmaps.model.OtherUsersLocationModel;
import de.stephanlindauer.criticalmaps.model.OwnLocationModel;
import de.stephanlindauer.criticalmaps.overlays.LocationMarker;
import de.stephanlindauer.criticalmaps.prefs.SharedPrefsKeys;
import de.stephanlindauer.criticalmaps.provider.EventBus;
import de.stephanlindauer.criticalmaps.utils.AlertBuilder;
import de.stephanlindauer.criticalmaps.utils.MapViewUtils;
import info.metadude.android.typedpreferences.BooleanPreference;

public class MapFragment extends Fragment {
    private final static String KEY_MAP_ZOOMLEVEL = "map_zoomlevel";
    private final static String KEY_MAP_POSITION = "map_position";
    private final static String KEY_MAP_ORIENTATION = "map_orientation";
    private final static String KEY_INITIAL_LOCATION_SET = "initial_location_set";

    private final static double DEFAULT_ZOOM_LEVEL = 12;
    private final static double NO_GPS_PERMISSION_ZOOM_LEVEL = 3;

    @Inject
    OwnLocationModel ownLocationModel;

    @Inject
    OtherUsersLocationModel otherUsersLocationModel;

    @Inject
    EventBus eventBus;

    @Inject
    LocationUpdateManager locationUpdateManager;

    @Inject
    ShowGpxHandler showGpxHandler;

    @Inject
    SharedPreferences sharedPreferences;

    private MapView mapView;
    private InfoWindow observerInfoWindow;

    private final GeoPoint defaultGeoPoint = new GeoPoint(52.499571, 13.4140875, 15);
    private boolean isInitialLocationSet = false;
    private ObjectAnimator gpsSearchingAnimator;

    // cache drawables
    private Drawable locationIcon;
    private Drawable ownLocationIcon;
    private Drawable ownLocationIconObserver;

    private FragmentMapBinding binding;

    private final View.OnClickListener centerLocationOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (ownLocationModel.ownLocation != null)
                animateToLocation(ownLocationModel.ownLocation);
        }
    };

    private final View.OnClickListener rotationNorthOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            float currentRotation = mapView.getMapOrientation() % 360;

            if (currentRotation == 0.0f) {
                // no animation required; also works around bug where map does a full rotation
                // because of mapView wrapping 360° to 0° while View allows 360°
                return;
            }

            if (currentRotation < 0.0f) {
                currentRotation = 360.0f + currentRotation;
                binding.mapSetNorthFab.setRotation(currentRotation);
                mapView.setMapOrientation(currentRotation);
            }

            float destinationRotation = currentRotation > 180.0f ? 360.0f : 0.0f;
            ViewCompat.animate(binding.mapSetNorthFab)
                    .rotation(destinationRotation)
                    .setDuration(300L)
                    .setUpdateListener(view -> mapView.setMapOrientation(view.getRotation()))
                    .start();
        }
    };

    private final View.OnClickListener noGpsOnClickListener = v -> AlertBuilder.show(getActivity(),
            R.string.map_no_gps_title,
            R.string.map_no_gps_text);

    private final View.OnClickListener gpsDisabledOnClickListener =
            v -> AlertBuilder.show(getActivity(),
                    R.string.map_gps_disabled_title,
                    R.string.map_gps_disabled_text);

    private final View.OnClickListener gpsNoPermissionsOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            locationUpdateManager.requestPermission();
        }
    };

    private final View.OnClickListener gpsPermissionsPermanentlyDeniedOnClickListener = v ->
            new AlertDialog.Builder(getActivity(), R.style.AlertDialogTheme)
                    .setTitle(R.string.map_gps_permissions_permanently_denied_title)
                    .setMessage(R.string.map_gps_permissions_permanently_denied_text)
                    .setNegativeButton(R.string.no, null)
                    .setPositiveButton(R.string.permissions_open_settings, (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", getActivity().getPackageName(), null));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    })
                    .create()
                    .show();

    private final View.OnClickListener searchingForLocationOnClickListener = v ->
            Toast.makeText(getActivity(), R.string.map_searching_for_location, Toast.LENGTH_SHORT)
                    .show();

    private final SharedPreferences.OnSharedPreferenceChangeListener observerModeOnSharedPreferenceChangeListener =
            (sharedPreferences, key) -> {
                if (SharedPrefsKeys.OBSERVER_MODE_ACTIVE.equals(key)) {
                    refreshView();
                }
            };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        binding = FragmentMapBinding.inflate(inflater, container, false);

        //noinspection ConstantConditions
        locationIcon = AppCompatResources.getDrawable(getActivity(), R.drawable.ic_map_marker);
        ownLocationIcon = AppCompatResources.getDrawable(
                getActivity(), R.drawable.ic_map_marker_own);
        ownLocationIconObserver = AppCompatResources.getDrawable(
                getActivity(), R.drawable.ic_map_marker_observer);

        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(final Bundle savedState) {
        super.onActivityCreated(savedState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            adjustToWindowsInsets();
        }

        App.components().inject(this);

        binding.mapOsmNoticeText.setMovementMethod(LinkMovementMethod.getInstance());

        mapView = MapViewUtils.createMapView(getActivity());
        binding.mapContainerLayout.addView(mapView);

        observerInfoWindow = MapViewUtils.createObserverInfoWindow(mapView);

        binding.mapSetCenterFab.setOnClickListener(centerLocationOnClickListener);
        binding.mapSetNorthFab.setOnClickListener(rotationNorthOnClickListener);

        binding.mapNoDataConnectivityFab.setOnClickListener(v -> AlertBuilder.show(getActivity(),
                R.string.map_no_internet_connection_title,
                R.string.map_no_internet_connection_text));

        if (new BooleanPreference(sharedPreferences, SharedPrefsKeys.DISABLE_MAP_ROTATION).get()) {
            binding.mapSetNorthFab.setVisibility(View.GONE);
        } else {
            RotationGestureOverlay rotationGestureOverlay = new RotationGestureOverlay(mapView) {
                @Override
                public void onRotate(float deltaAngle) {
                    super.onRotate(deltaAngle);
                    binding.mapSetNorthFab.setRotation(mapView.getMapOrientation());
                }
            };
            rotationGestureOverlay.setEnabled(true);
            mapView.setMultiTouchControls(true);
            mapView.getOverlays().add(rotationGestureOverlay);
        }

        if (savedState != null) {
            Double zoomLevel = (Double) savedState.get(KEY_MAP_ZOOMLEVEL);
            GeoPoint position = savedState.getParcelable(KEY_MAP_POSITION);
            Float orientation = (Float) savedState.get(KEY_MAP_ORIENTATION);

            if (zoomLevel != null && position != null && orientation != null) {
                mapView.getController().setZoom(zoomLevel);
                if (!new BooleanPreference(sharedPreferences, SharedPrefsKeys.DISABLE_MAP_ROTATION)
                        .get()) {
                    mapView.setMapOrientation(orientation);
                }
                setToLocation(position);
            }

            isInitialLocationSet = savedState.getBoolean(KEY_INITIAL_LOCATION_SET, false);
        }
        binding.mapSetNorthFab.setRotation(mapView.getMapOrientation());

        showGpxHandler.showGpx(mapView);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void adjustToWindowsInsets() {
        // No-op on < API21
        ViewCompat.setOnApplyWindowInsetsListener(binding.mapOverlayContainerLayout, (v, insets) -> {
            // inset the map overlays for the status bar
            v.setPaddingRelative(
                    v.getPaddingStart(), v.getPaddingTop() + insets.getSystemWindowInsetTop(),
                    v.getPaddingEnd(), v.getPaddingBottom());

            // clear this listener so insets aren't re-applied
            ViewCompat.setOnApplyWindowInsetsListener(binding.mapOverlayContainerLayout, null);
            return insets;
        });

        // without this insets aren't reapplied on fragment changes
        ViewCompat.requestApplyInsets(binding.mapOverlayContainerLayout);
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
            if (new BooleanPreference(
                    sharedPreferences, SharedPrefsKeys.OBSERVER_MODE_ACTIVE).get()) {
                ownMarker.setIcon(ownLocationIconObserver);
                ownMarker.setInfoWindow(observerInfoWindow);
                // since we're currently creating new markers on every refresh, this workaround
                // is needed to update the info window's position if it's open
                if (observerInfoWindow.isOpen()) {
                    ownMarker.showInfoWindow();
                }
            } else {
                observerInfoWindow.close();
                ownMarker.setIcon(ownLocationIcon);
            }
            mapView.getOverlays().add(ownMarker);
        }

        mapView.invalidate();
    }

    @Override
    public void onResume() {
        super.onResume();
        eventBus.register(this);

        sharedPreferences.registerOnSharedPreferenceChangeListener(
                observerModeOnSharedPreferenceChangeListener);

        if (locationUpdateManager.checkPermission()) {
            locationUpdateManager.startListening();
        } else {
            zoomToLocation(defaultGeoPoint, NO_GPS_PERMISSION_ZOOM_LEVEL);
        }
    }

    private void handleFirstLocationUpdate() {
        setGpsStatusFixed();
        zoomToLocation(ownLocationModel.ownLocation, DEFAULT_ZOOM_LEVEL);
        isInitialLocationSet = true;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putDouble(KEY_MAP_ZOOMLEVEL, mapView.getZoomLevelDouble());
        outState.putParcelable(KEY_MAP_POSITION, (GeoPoint) mapView.getMapCenter());
        outState.putFloat(KEY_MAP_ORIENTATION, mapView.getMapOrientation());
        outState.putBoolean(KEY_INITIAL_LOCATION_SET, isInitialLocationSet);
    }

    @Override
    public void onPause() {
        super.onPause();
        eventBus.unregister(this);

        sharedPreferences.unregisterOnSharedPreferenceChangeListener(
                observerModeOnSharedPreferenceChangeListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // properly closes the cache db since it's stored in a static field in osmdroid...
        try {
            ((SqlTileWriter) mapView.getTileProvider().getTileWriter()).refreshDb();
        } catch (Exception ignored) {
            // nothing we can do
        }
        mapView = null;
        binding = null;
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
        if (e.isConnected) {
            binding.mapNoDataConnectivityFab.hide();
        } else {
            binding.mapNoDataConnectivityFab.show();
        }
    }

    @Subscribe
    public void handleGpsStatusChangedEvent(GpsStatusChangedEvent e) {
        if (e.status == GpsStatusChangedEvent.Status.NONEXISTENT) {
            setGpsStatusNonexistent();
        } else if (e.status == GpsStatusChangedEvent.Status.DISABLED) {
            setGpsStatusDisabled();
        } else if (e.status == GpsStatusChangedEvent.Status.PERMISSION_PERMANENTLY_DENIED) {
            setGpsStatusPermissionsPermanentlyDenied();
        } else if (e.status == GpsStatusChangedEvent.Status.NO_PERMISSIONS) {
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
        setGpsStatusCommon(R.color.map_fab_warning, R.drawable.ic_map_no_gps,
                noGpsOnClickListener);
    }

    private void setGpsStatusDisabled() {
        cancelGpsSearchingAnimationIfRunning();
        setGpsStatusCommon(R.color.map_fab_warning, R.drawable.ic_map_no_gps,
                gpsDisabledOnClickListener);
    }

    private void setGpsStatusNoPermissions() {
        cancelGpsSearchingAnimationIfRunning();
        setGpsStatusCommon(R.color.map_fab_warning, R.drawable.ic_map_no_gps,
                gpsNoPermissionsOnClickListener);
    }

    private void setGpsStatusPermissionsPermanentlyDenied() {
        cancelGpsSearchingAnimationIfRunning();
        setGpsStatusCommon(R.color.map_fab_warning, R.drawable.ic_map_no_gps,
                gpsPermissionsPermanentlyDeniedOnClickListener);
    }

    private void setGpsStatusFixed() {
        cancelGpsSearchingAnimationIfRunning();
        setGpsStatusCommon(R.color.colorSecondary, R.drawable.ic_map_center_location,
                centerLocationOnClickListener);
    }

    private void setGpsStatusSearching() {
        cancelGpsSearchingAnimationIfRunning();
        setGpsStatusCommon(R.color.map_fab_searching, R.drawable.ic_map_gps_not_fixed,
                searchingForLocationOnClickListener);

        gpsSearchingAnimator = (ObjectAnimator) AnimatorInflater.loadAnimator(
                getActivity(),
                R.animator.map_gps_fab_searching_animation);
        gpsSearchingAnimator.setTarget(binding.mapSetCenterFab);
        gpsSearchingAnimator.start();
    }

    private void setGpsStatusCommon(@ColorRes int colorResId, @DrawableRes int iconResId,
                                    View.OnClickListener onClickListener) {
        //noinspection ConstantConditions
        binding.mapSetCenterFab.setBackgroundTintList(
                ContextCompat.getColorStateList(getActivity(), colorResId));
        binding.mapSetCenterFab.setImageResource(iconResId);
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            binding.mapSetCenterFab.refreshDrawableState();
        }
        binding.mapSetCenterFab.setOnClickListener(onClickListener);
    }

    private void cancelGpsSearchingAnimationIfRunning() {
        if (gpsSearchingAnimator != null) {
            gpsSearchingAnimator.cancel();
            binding.mapSetCenterFab.setAlpha(1.0f);
        }
    }

    private void zoomToLocation(final GeoPoint location, final double zoomLevel) {
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
