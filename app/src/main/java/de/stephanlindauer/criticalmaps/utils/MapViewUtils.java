package de.stephanlindauer.criticalmaps.utils;

import android.app.Activity;
import androidx.core.content.ContextCompat;
import android.view.ViewGroup;

import org.osmdroid.config.Configuration;
import org.osmdroid.config.IConfigurationProvider;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.modules.SqlTileWriter;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;

import java.io.File;

import de.stephanlindauer.criticalmaps.App;
import de.stephanlindauer.criticalmaps.BuildConfig;
import de.stephanlindauer.criticalmaps.R;
import de.stephanlindauer.criticalmaps.provider.StorageLocationProvider;
import timber.log.Timber;

public class MapViewUtils {
    
    final static OnlineTileSourceBase WIKIMEDIA = new XYTileSource("Wikimedia",
            1, 19, 256, ".png",
            new String[] {"https://maps.wikimedia.org/osm-intl/"},
            "Wikimedia maps | Map data © OpenStreetMap contributors");

    private MapViewUtils() {}

    public static MapView createMapView(Activity activity) {
        IConfigurationProvider configuration = Configuration.getInstance();

        StorageLocationProvider.StorageLocation storageLocation =
                App.components().storageProvider().getActiveStorageLocation();
        if (storageLocation == null) {
            storageLocation = App.components().storageProvider().getAndSaveBestStorageLocation();
        }
        File osmdroidBasePath = storageLocation.osmdroidBasePath;
        File osmdroidTileCache = storageLocation.osmdroidTilePath;

        Timber.d("Setting osmdroidBasePath to: %s", osmdroidBasePath.getAbsolutePath());
        configuration.setOsmdroidBasePath(osmdroidBasePath);
        Timber.d("Setting osmdroidTileCache to: %s", osmdroidTileCache.getAbsolutePath());
        configuration.setOsmdroidTileCache(osmdroidTileCache);

        setMaxCacheSize(configuration);

        // TODO Expiration! setExpirationExtendedDuration() OR setExpirationOverrideDuration()
        // TODO Add option to adjust

        configuration.setMapViewHardwareAccelerated(true);
        configuration.setUserAgentValue(BuildConfig.APPLICATION_ID + "/"
                + BuildConfig.VERSION_NAME + " " + org.osmdroid.library.BuildConfig.APPLICATION_ID
                + "/" + org.osmdroid.library.BuildConfig.VERSION_NAME
                + " (" + activity.getString(R.string.contact_email) + ")");

        MapTileProviderBasic mapnikTileProvider =
                new MapTileProviderBasic(activity.getApplicationContext(), WIKIMEDIA);

        MapView mapView = new MapView(activity, mapnikTileProvider);
        mapView.getZoomController().setVisibility(
                CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(1.0d);
        mapView.getController().setCenter(new GeoPoint(0.0d, 0.0d));
        mapView.setClickable(true);
        mapView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        mapView.setTilesScaledToDpi(true);
        mapView.getOverlayManager()
                .getTilesOverlay()
                .setLoadingBackgroundColor(
                        ContextCompat.getColor(activity, R.color.map_loading_tile_color));
        mapView.getOverlayManager()
                .getTilesOverlay()
                .setLoadingLineColor(
                        ContextCompat.getColor(activity, R.color.map_loading_line_color));

        return mapView;
    }

    private static void setMaxCacheSize(IConfigurationProvider configuration) {
        // code adapted from osmdroid's DefaulConfigurationProvider.load()
        long cacheSize = 0;
        File dbFile = new File(configuration.getOsmdroidTileCache().getAbsolutePath()
                + File.separator + SqlTileWriter.DATABASE_FILENAME);
        if (dbFile.exists()) {
            cacheSize = dbFile.length();
        }

        long freeSpace = configuration.getOsmdroidTileCache().getFreeSpace();
        Timber.d("cacheSize: %d", cacheSize);
        Timber.d("freeSpace: %d", freeSpace);
        Timber.d("getTileFileSystemCacheMaxBytes(): %d",
                configuration.getTileFileSystemCacheMaxBytes());

        if (configuration.getTileFileSystemCacheMaxBytes() > (freeSpace + cacheSize)) {
            configuration.setTileFileSystemCacheMaxBytes((long)((freeSpace + cacheSize) * 0.95));
            configuration.setTileFileSystemCacheTrimBytes((long)((freeSpace + cacheSize) * 0.90));
        }

        Timber.d("getTileFileSystemCacheMaxBytes(): %d",
                configuration.getTileFileSystemCacheMaxBytes());
        Timber.d("getTileFileSystemCacheTrimBytes(): %d",
                configuration.getTileFileSystemCacheTrimBytes());
    }
}
