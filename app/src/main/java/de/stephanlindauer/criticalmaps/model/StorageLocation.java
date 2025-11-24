package de.stephanlindauer.criticalmaps.model;

import android.os.StatFs;
import java.io.File;


public class StorageLocation {
    private final File dbFile;

    public StorageLocation(File path) {
        if (!path.exists()) {
            throw new IllegalArgumentException("Path does not exist or is read only: " + path);
        }

        dbFile = new File(path + File.separator + "mbgl-offline.db");
    }

    public long getFreeSpaceBytes() {
        return new StatFs(dbFile.getAbsolutePath()).getAvailableBytes();
    }

    public long getTotalSizeBytes() {
        return new StatFs(dbFile.getAbsolutePath()).getTotalBytes();
    }

    public long getUsedSpace() {
        return getTotalSizeBytes() - getFreeSpaceBytes();
    }

    public long getCacheSize() {
        long cacheSize = 0;
        if (dbFile.exists()) {
            cacheSize = dbFile.length();
        }

        return cacheSize;
    }
}
