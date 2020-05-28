package de.stephanlindauer.criticalmaps.utils;

import android.content.ContentResolver;
import android.content.Intent;

public class GpxUtils {

    public static void persistPermissionOnFile(Intent data, ContentResolver contentResolver) {
        final int takeFlags = data.getFlags()
                & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        contentResolver.takePersistableUriPermission(data.getData(), takeFlags);
    }
}
