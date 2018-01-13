package de.stephanlindauer.criticalmaps.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.os.EnvironmentCompat;
import android.view.ViewGroup;

import org.osmdroid.config.Configuration;
import org.osmdroid.config.IConfigurationProvider;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.modules.SqlTileWriter;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import de.stephanlindauer.criticalmaps.App;
import de.stephanlindauer.criticalmaps.BuildConfig;
import de.stephanlindauer.criticalmaps.R;
import de.stephanlindauer.criticalmaps.prefs.SharedPrefsKeys;
import de.stephanlindauer.criticalmaps.provider.MapConfigurationProvider;
import info.metadude.android.typedpreferences.StringPreference;
import timber.log.Timber;

public class MapViewUtils {
    private MapViewUtils() {}

    public static MapView createMapView(Activity activity) {
        Configuration.setConfigurationProvider(new MapConfigurationProvider());
        IConfigurationProvider configuration = Configuration.getInstance();

        SharedPreferences sharedPrefs = App.components().sharedPreferences();
        StringPreference osmdroidBasePathPref =
                new StringPreference(sharedPrefs, SharedPrefsKeys.OSMDROID_BASE_PATH);

        File osmdroidBasePath =
                checkAndGetOsmdroidBasePathFile(activity, osmdroidBasePathPref.get());

        osmdroidBasePathPref.set(osmdroidBasePath.getAbsolutePath());

        File osmdroidTileCache = new File(osmdroidBasePath, "tiles");
        //noinspection ResultOfMethodCallIgnored
        osmdroidTileCache.mkdirs();
        Timber.d("Setting osmdroidBasePath to: %s", osmdroidBasePath.getAbsolutePath());
        configuration.setOsmdroidBasePath(osmdroidBasePath);
        Timber.d("Setting osmdroidTileCache to: %s", osmdroidTileCache.getAbsolutePath());
        configuration.setOsmdroidTileCache(osmdroidTileCache);

        setMaxCacheSize(configuration);

        configuration.setMapViewHardwareAccelerated(true);
        configuration.setUserAgentValue(BuildConfig.APPLICATION_ID + "/"
                + BuildConfig.VERSION_NAME + " " + org.osmdroid.library.BuildConfig.APPLICATION_ID
                + "/" + org.osmdroid.library.BuildConfig.VERSION_NAME);

        MapTileProviderBasic mapnikTileProvider =
                new MapTileProviderBasic(activity, TileSourceFactory.MAPNIK);

        MapView mapView = new MapView(activity, mapnikTileProvider);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(1);
        mapView.getController().setCenter(new GeoPoint(0.0d, 0.0d));
        mapView.setClickable(true);
        mapView.setBuiltInZoomControls(true);
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

    @NonNull
    private static File checkAndGetOsmdroidBasePathFile(Context context, String savedBasePath) {
        Timber.d("Saved path is: %s", savedBasePath);
        File savedBasePathFile = new File(savedBasePath);
        //noinspection ResultOfMethodCallIgnored
        savedBasePathFile.mkdirs();

        boolean useSavedDir = isPathAvailableForWrite(context, savedBasePathFile);

        if (!useSavedDir) {
            Timber.d("Saved path unavailable, finding best storage");
            savedBasePathFile = new File(getBestStorageLocation(context), "osmdroid");
            //noinspection ResultOfMethodCallIgnored
            savedBasePathFile.mkdirs();
        }

        Timber.d("Using path: %s", savedBasePathFile.getAbsolutePath());
        return savedBasePathFile;
    }

    private static File getBestStorageLocation(Context context) {
        Timber.d("Finding best storage Location.");

        ArrayList<File> storageDirs = new ArrayList<>(3);
        storageDirs.add(context.getFilesDir());

        File[] externalDirs = ContextCompat.getExternalFilesDirs(context, null);
        for (File externalDir : externalDirs) {
            String state = EnvironmentCompat.getStorageState(externalDir);
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                storageDirs.add(externalDir);
            }
        }

        File bestLocation = null;
        long freeSpace = 0;
        for (File storageDir : storageDirs) {
            Timber.d("Found available storage: " + storageDir.getAbsolutePath()
                    + ", free space: " + storageDir.getFreeSpace());
            if (storageDir.getFreeSpace() > freeSpace) {
                bestLocation = storageDir;
            }
        }

        Timber.d("Determined best storage location: %s",
                bestLocation != null ? bestLocation.getAbsolutePath() : "null");
        return bestLocation;
    }

    private static boolean isPathAvailableForWrite(Context context, File path) {
        boolean pathAvailable = false;
        if (path.exists()) {
            if (!isPathInInternalFilesDir(context, path)) {
                String state = EnvironmentCompat.getStorageState(path);
                pathAvailable = Environment.MEDIA_MOUNTED.equals(state);
            } else {
                // result of getFilesDir() is guaranteed to be writable
                pathAvailable = true;
            }
        }
        return pathAvailable;
    }

    private static boolean isPathInInternalFilesDir(Context context, File path) {
        String canonicalPath;
        String canonicalInternal;
        try {
            canonicalPath = path.getCanonicalPath();
            canonicalInternal = context.getFilesDir().getCanonicalPath();
        } catch (IOException e) {
            return false;
        }
        return canonicalPath.startsWith(canonicalInternal);
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

        if (configuration.getTileFileSystemCacheMaxBytes() > (freeSpace + cacheSize)) {
            configuration.setTileFileSystemCacheMaxBytes((long)((freeSpace + cacheSize) * 0.95));
            configuration.setTileFileSystemCacheTrimBytes((long)((freeSpace + cacheSize) * 0.90));
        }
    }
}
