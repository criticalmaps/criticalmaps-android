package de.stephanlindauer.criticalmaps.utils;

import android.os.Environment;

import java.io.File;
import java.util.UUID;

public class ImageUtils {

    private ImageUtils() {}

    public static File getNewOutputImageFile() {
        File mediaStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "CriticalMaps");

        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            return null;
        }

        final String id = UUID.randomUUID().toString().replace("-", "");
        return new File(mediaStorageDir.getPath() + File.separator + id + ".jpg");
    }
}
