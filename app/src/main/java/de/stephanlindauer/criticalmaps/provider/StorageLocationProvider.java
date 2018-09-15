package de.stephanlindauer.criticalmaps.provider;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.os.EnvironmentCompat;

import org.osmdroid.tileprovider.modules.SqlTileWriter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Singleton;

import de.stephanlindauer.criticalmaps.App;
import de.stephanlindauer.criticalmaps.R;
import de.stephanlindauer.criticalmaps.prefs.SharedPrefsKeys;
import info.metadude.android.typedpreferences.StringPreference;
import timber.log.Timber;

@Singleton
public class StorageLocationProvider {

    private final App app;
    private final StringPreference osmdroidBasePathPref;

    @SuppressWarnings("WeakerAccess")
    @Inject
    public StorageLocationProvider(App app, SharedPreferences sharedPreferences) {
        this.app = app;

        osmdroidBasePathPref =
                new StringPreference(sharedPreferences, SharedPrefsKeys.OSMDROID_BASE_PATH);
    }

    @NonNull
    public ArrayList<StorageLocation> getAllWritableStorageLocations() {
        ArrayList<StorageLocation> availableStorageLocations = new ArrayList<>(4);

        Timber.d("Finding storage locations.");
        ArrayList<File> storageDirs = new ArrayList<>(4);
        storageDirs.add(app.getFilesDir());

        File[] externalDirs = ContextCompat.getExternalFilesDirs(app, null);
        for (File externalDir : externalDirs) {
            // "Returned paths may be null if a storage device is unavailable."
            if (externalDir == null) {
                Timber.d("An external storage location is null (=unavailable), skipping.");
                continue;
            }

            String state = EnvironmentCompat.getStorageState(externalDir);
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                storageDirs.add(externalDir);
            }
        }

        for (File storageDir : storageDirs) {
            Timber.d("Found storage location: %s", storageDir.getAbsolutePath());
            availableStorageLocations.add(new StorageLocation(app, storageDir));
        }

        return availableStorageLocations;
    }

    @Nullable
    public StorageLocation getActiveStorageLocation() {
        String savedOsmdroidBasePath = osmdroidBasePathPref.get();
        Timber.d("Saved path is: %s", savedOsmdroidBasePath);

        String savedFilePath = savedOsmdroidBasePath.replace("/osmdroid", "");

        try {
            return new StorageLocation(app, new File(savedFilePath));
        } catch (IllegalArgumentException e) {
            Timber.d(e);
            return null;
        }
    }

    public StorageLocation getAndSaveBestStorageLocation() {
        Timber.d("Finding best storage location.");

        ArrayList<StorageLocation> storageLocations = getAllWritableStorageLocations();

        StorageLocation bestLocation = null;
        long bestFreeSpace = 0;
        for (StorageLocation storageLocation : storageLocations) {
            Timber.d("Available storage: " + storageLocation.storagePath
                    + ", free space: " + storageLocation.freeSpace);
            if (storageLocation.freeSpace > bestFreeSpace) {
                bestLocation = storageLocation;
                bestFreeSpace = storageLocation.freeSpace;
            }
        }

        Timber.d("Determined best storage location: %s",
                bestLocation != null ? bestLocation.storagePath : "null");

        setActiveStorageLocation(bestLocation);

        return bestLocation;
    }

    public void setActiveStorageLocation(StorageLocation storageLocation) {
        @SuppressWarnings("ConstantConditions")
        File osmdroidBasePath = new File(storageLocation.storagePath, "osmdroid");
        //noinspection ResultOfMethodCallIgnored
        osmdroidBasePath.mkdirs();
        File osmdroidTilePath = new File(osmdroidBasePath, "tiles");
        //noinspection ResultOfMethodCallIgnored
        osmdroidTilePath.mkdirs();

        storageLocation.osmdroidBasePath = osmdroidBasePath;
        storageLocation.osmdroidTilePath = osmdroidTilePath;
        osmdroidBasePathPref.set(storageLocation.osmdroidBasePath.getAbsolutePath());
        Timber.d("Saved location: %s", osmdroidBasePath.getAbsolutePath());
    }

    private static long getFreeSpaceBytes(File storageDir) {
        long freeSpace;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            // gives more accurate information
            freeSpace = new StatFs(storageDir.getAbsolutePath()).getAvailableBytes();
        } else {
            freeSpace = storageDir.getFreeSpace();
        }

        return freeSpace;
    }

    private static long getTotalSizeBytes(File storageDir) {
        long totalSize;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            // gives more accurate information
            totalSize = new StatFs(storageDir.getAbsolutePath()).getTotalBytes(); //TODO
        } else {
            totalSize = storageDir.getTotalSpace(); //TODO
        }

        return totalSize;
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

    private static boolean isPathOnRemovableStorage(File path) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return Environment.isExternalStorageRemovable(path);
        } else {
            return Environment.isExternalStorageRemovable();
        }
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

    public static class StorageLocation {
        public String displayName;
        public File storagePath;
        public File osmdroidBasePath;
        public File osmdroidTilePath;
        public long totalSize;
        public long freeSpace;
        public long usedSpace;

        private File dbFile;

        public StorageLocation(Context context, File path) {
            if (!path.exists() || !isPathAvailableForWrite(context, path)) {
                throw new IllegalArgumentException("Path does not exist or is read only: " + path);
            }

            this.storagePath = path;
            this.osmdroidBasePath = new File(storagePath, "osmdroid");
            this.osmdroidTilePath = new File(osmdroidBasePath, "tiles");

            if (isPathInInternalFilesDir(context, storagePath)) {
                displayName = context.getString(R.string.storage_name_internal);
            } else if (isPathOnRemovableStorage(storagePath)) {
                displayName = context.getString(R.string.storage_name_external);
            } else {
                displayName = context.getString(R.string.storage_name_emulated);
            }

            totalSize = getTotalSizeBytes(storagePath);
            freeSpace = getFreeSpaceBytes(storagePath);
            usedSpace = totalSize - freeSpace;

            dbFile = new File(osmdroidTilePath.getAbsolutePath() + File.separator
                    + SqlTileWriter.DATABASE_FILENAME);
        }

        public long getCacheSize() {
            long cacheSize = 0;
            if (dbFile.exists()) {
                cacheSize = dbFile.length();
            }

            return cacheSize;
        }

        public boolean clearCache() {
            return dbFile.delete();
        }
    }
}
