package de.stephanlindauer.criticalmaps.utils;

import android.os.Environment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImageUtils {

    private ImageUtils() {}

    public static File getNewOutputImageFile() {
        File mediaStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "CriticalMaps");

        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            return null;
        }

        final String timestamp =
                new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(new Date());
        final String filename = "CriticalMaps-" + timestamp + ".jpg";
        return new File(mediaStorageDir.getPath() + File.separator + filename);
    }
}
