package de.stephanlindauer.criticalmaps.fragments;

import static de.stephanlindauer.criticalmaps.events.Events.NEW_LOCATION_EVENT;
import static de.stephanlindauer.criticalmaps.utils.MapViewUtils.dpToInt;

import android.animation.AnimatorInflater;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Outline;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.Toast;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.gson.JsonObject;
import com.squareup.otto.Subscribe;

import org.maplibre.android.camera.CameraPosition;
import org.maplibre.android.camera.CameraUpdateFactory;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.Style;
import org.maplibre.android.style.sources.GeoJsonSource;
import org.maplibre.geojson.Feature;
import org.maplibre.geojson.FeatureCollection;
import org.maplibre.geojson.Point;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;
import javax.inject.Provider;

import de.stephanlindauer.criticalmaps.App;
import de.stephanlindauer.criticalmaps.R;
import de.stephanlindauer.criticalmaps.databinding.FragmentMapBinding;
import de.stephanlindauer.criticalmaps.events.GpsStatusChangedEvent;
import de.stephanlindauer.criticalmaps.events.NetworkConnectivityChangedEvent;
import de.stephanlindauer.criticalmaps.events.NewLocationEvent;
import de.stephanlindauer.criticalmaps.events.NewServerResponseEvent;
import de.stephanlindauer.criticalmaps.handler.GetLocationHandler;
// import de.stephanlindauer.criticalmaps.handler.ShowGpxHandler;
import de.stephanlindauer.criticalmaps.managers.LocationUpdateManager;
import de.stephanlindauer.criticalmaps.model.OtherUsersLocationModel;
import de.stephanlindauer.criticalmaps.model.OwnLocationModel;
import de.stephanlindauer.criticalmaps.prefs.SharedPrefsKeys;
import de.stephanlindauer.criticalmaps.provider.EventBus;
import de.stephanlindauer.criticalmaps.utils.AlertBuilder;
import de.stephanlindauer.criticalmaps.utils.MapViewUtils;
import info.metadude.android.typedpreferences.BooleanPreference;


public class MapFragment extends Fragment {
    private final static String KEY_INITIAL_LOCATION_SET = "initial_location_set";

    private final static double DEFAULT_ZOOM_LEVEL = 12;
    private final static double NO_GPS_PERMISSION_ZOOM_LEVEL = 3;
    private final int SERVER_SYNC_INTERVAL = 30 * 1000; // 30 sec

    @Inject
    Provider<GetLocationHandler> getLocationHandler;

    @Inject
    OwnLocationModel ownLocationModel;

    @Inject
    OtherUsersLocationModel otherUsersLocationModel;

    @Inject
    EventBus eventBus;

    @Inject
    LocationUpdateManager locationUpdateManager;

    /*
    @Inject
    ShowGpxHandler showGpxHandler;
    */

    @Inject
    SharedPreferences sharedPreferences;

    private MapView mapView;
    private MapLibreMap map;
    private Style mapStyle;
    // private InfoWindow observerInfoWindow;

    private final LatLng defaultGeoPoint = new LatLng(52.499571, 13.4140875);
    private boolean isInitialLocationSet = false;
    private ObjectAnimator gpsSearchingAnimator;

    private FragmentMapBinding binding;

    private Timer timerGetLocation;

    private final View.OnClickListener centerLocationOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (ownLocationModel.ownLocation != null) {
                CameraPosition cameraPosition =
                        new CameraPosition.Builder().target(ownLocationModel.ownLocation).build();
                animateToCameraPosition(cameraPosition);
            }
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

        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(final Bundle savedState) {
        super.onActivityCreated(savedState);

        adjustToWindowsInsets();

        App.components().inject(this);

        binding.mapOsmNoticeText.setMovementMethod(LinkMovementMethod.getInstance());

        mapView = MapViewUtils.createMapView(getActivity());
        binding.mapContainerLayout.addView(mapView);

        mapView.getMapAsync(map -> {
            this.map = map;

            // Set shadow on compass
            // NOTE: Needs to be before updating uiSettings below, otherwise compassView will be null!
            View compassView = mapView.findViewWithTag("compassView");
            compassView.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setOval(0, 0, view.getWidth(), view.getHeight());
                }
            });
            compassView.setElevation(dpToInt(6));

            // Set compass y position to align it with other fabs
            View setCenterFab = binding.getRoot().findViewById(R.id.map_set_center_fab);
            float setCenterFabY = setCenterFab.getY();
            map.getUiSettings().setCompassMargins(
                    dpToInt(18) + binding.mapOverlayContainerLayout.getPaddingLeft(), // when started
                    Math.round(setCenterFabY + dpToInt(18)),
                    0,
                    0);

            // TODO: save sprites and glyphs as local asset,
            //       see https://github.com/maplibre/flutter-maplibre-gl/issues/338
            Style.Builder styleBuilder = new Style.Builder().fromUri("asset://styles/versatilescolorful.json");

            map.setStyle(styleBuilder, style -> {
                mapStyle = style;
                MapViewUtils.setupSourcesAndLayers(getActivity(), mapStyle);

                // trigger fake location update in case fix was acquired already during map init
                handleNewLocation(NEW_LOCATION_EVENT);
            });
        });
        mapView.onCreate(savedState); // TODO: Should be onViewCreated

        // observerInfoWindow = MapViewUtils.createObserverInfoWindow(mapView);

        binding.mapSetCenterFab.setOnClickListener(centerLocationOnClickListener);

        binding.mapNoDataConnectivityFab.setOnClickListener(v -> AlertBuilder.show(getActivity(),
                R.string.map_no_internet_connection_title,
                R.string.map_no_internet_connection_text));

        if (savedState != null) {
            isInitialLocationSet = savedState.getBoolean(KEY_INITIAL_LOCATION_SET, false);
        }

        // showGpxHandler.showGpx(mapView);

        if (!LocationUpdateManager.checkPermission()) {
            zoomToLocation(defaultGeoPoint, NO_GPS_PERMISSION_ZOOM_LEVEL);
        }
    }

    private void adjustToWindowsInsets() {
        // inset the map overlays for the status bar
        final int originalContainerPaddingTop = binding.mapOverlayContainerLayout.getPaddingTop();
        final int originalContainerPaddingLeft = binding.mapOverlayContainerLayout.getPaddingLeft();
        final int originalContainerPaddingRight = binding.mapOverlayContainerLayout.getPaddingRight();
        ViewCompat.setOnApplyWindowInsetsListener(binding.mapOverlayContainerLayout, (v, insets) -> {
            Insets systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(
                    originalContainerPaddingLeft + systemBarsInsets.left,
                    originalContainerPaddingTop + systemBarsInsets.top,
                    originalContainerPaddingRight + systemBarsInsets.right,
                    v.getPaddingBottom());

            if (map != null) {
                map.getUiSettings().setCompassMargins(
                        dpToInt(18) + binding.mapOverlayContainerLayout.getPaddingLeft(), // when started
                        map.getUiSettings().getCompassMarginTop(),
                        0,
                        0);
            }

            return insets;
        });

        // inset attribution
        ViewGroup.MarginLayoutParams lpNoticeText =
                (ViewGroup.MarginLayoutParams) binding.mapOsmNoticeText.getLayoutParams();
        final int originalNoticeTextBottomMargin = lpNoticeText.bottomMargin;
        ViewCompat.setOnApplyWindowInsetsListener(binding.mapOsmNoticeText, (v, insets) -> {
            Insets systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            lpNoticeText.bottomMargin = originalNoticeTextBottomMargin + systemBarsInsets.bottom;
            v.setLayoutParams(lpNoticeText);

            return WindowInsetsCompat.CONSUMED;
        });
    }

    private FeatureCollection getOtherUsersFeatureCollection() {
        ArrayList<Feature> features = new ArrayList<>();

        otherUsersLocationModel.getOtherUsersLocations().forEach((deviceId, location) -> {
            JsonObject properties = new JsonObject();
            properties.addProperty("deviceId", deviceId);
            features.add(Feature.fromGeometry(Point.fromLngLat(location.getLongitude(), location.getLatitude()), properties));
        });
        return FeatureCollection.fromFeatures(features);
    }

    private FeatureCollection getOwnUserFeatureCollection() {
        ArrayList<Feature> features = new ArrayList<>();

        LatLng location = ownLocationModel.ownLocation;
        JsonObject properties = new JsonObject();
        properties.addProperty("deviceId", "own");
        features.add(Feature.fromGeometry(Point.fromLngLat(location.getLongitude(), location.getLatitude()), properties));

        return FeatureCollection.fromFeatures(features);
    }

    private void refreshView() {
        if (map == null || mapStyle == null) {
            return;
        }

        FeatureCollection otherUsersFeatures = getOtherUsersFeatureCollection();
        GeoJsonSource otherUsersLocationsSource =
                (GeoJsonSource) mapStyle.getSource("otherUsersLocationsSource");
        otherUsersLocationsSource.setGeoJson(otherUsersFeatures);

        if (ownLocationModel.ownLocation != null) {
            FeatureCollection ownUserFeatures = getOwnUserFeatureCollection();
            FeatureCollection emptyFeatures =
                    FeatureCollection.fromJson("{\"type\":\"FeatureCollection\",\"features\":[]}");

            GeoJsonSource ownUserLocationSourceObserver =
                    (GeoJsonSource) mapStyle.getSource("ownUserLocationSourceObserver");
            GeoJsonSource ownUserLocationSource =
                    (GeoJsonSource) mapStyle.getSource("ownUserLocationSource");

            if (new BooleanPreference(sharedPreferences, SharedPrefsKeys.OBSERVER_MODE_ACTIVE).get()) {
                ownUserLocationSource.setGeoJson(emptyFeatures);
                ownUserLocationSourceObserver.setGeoJson(ownUserFeatures);
            } else {
                ownUserLocationSource.setGeoJson(ownUserFeatures);
                ownUserLocationSourceObserver.setGeoJson(emptyFeatures);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();

        eventBus.register(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(
                observerModeOnSharedPreferenceChangeListener);

        startGetLocationTimer();
    }

    private void handleFirstLocationUpdate() {
        if (map == null) {
            return;
        }
        setGpsStatusFixed();
        zoomToLocation(ownLocationModel.ownLocation, DEFAULT_ZOOM_LEVEL);
        isInitialLocationSet = true;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mapView != null && !mapView.isDestroyed()) {
            mapView.onSaveInstanceState(outState);
        }
        outState.putBoolean(KEY_INITIAL_LOCATION_SET, isInitialLocationSet);
    }

    @Override
    public void onPause() {
        super.onPause();

        mapView.onPause();
        stopGetLocationTimer();
        eventBus.unregister(this);
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(
                observerModeOnSharedPreferenceChangeListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null && !mapView.isDestroyed()) {
            mapView.onLowMemory();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mapView.onDestroy();
        mapView = null;
        map = null;
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

        if (e.isConnected && timerGetLocation == null) {
            startGetLocationTimer();
        } else {
            stopGetLocationTimer();
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
        binding.mapSetCenterFab.setOnClickListener(onClickListener);
    }

    private void cancelGpsSearchingAnimationIfRunning() {
        if (gpsSearchingAnimator != null) {
            gpsSearchingAnimator.cancel();
            binding.mapSetCenterFab.setAlpha(1.0f);
        }
    }

    private void zoomToLocation(final LatLng location, final double zoomLevel) {
        CameraPosition cameraPosition =
                new CameraPosition.Builder().target(location).zoom(zoomLevel).build();
        animateToCameraPosition(cameraPosition);
    }

    private void animateToCameraPosition(final CameraPosition cameraPosition) {
        if (map == null) {
            return;
        }

        map.animateCamera(
                CameraUpdateFactory.newCameraPosition(cameraPosition), 2000, null);
    }

    private void startGetLocationTimer() {
        stopGetLocationTimer();

        timerGetLocation = new Timer();

        TimerTask timerTaskPullServer = new TimerTask() {
            @Override
            public void run() {
                getLocationHandler.get().execute();
            }
        };
        timerGetLocation.schedule(timerTaskPullServer, 0, SERVER_SYNC_INTERVAL);
    }

    private void stopGetLocationTimer() {
        if (timerGetLocation != null) {
            timerGetLocation.cancel();
            timerGetLocation = null;
        }
    }
}
