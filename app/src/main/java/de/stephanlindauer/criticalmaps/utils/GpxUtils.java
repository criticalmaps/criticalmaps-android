package de.stephanlindauer.criticalmaps.utils;

import android.content.ContentResolver;
import android.content.Intent;
import android.os.Build;

public class GpxUtils {

    public static void persistPermissionOnFile(Intent data, ContentResolver contentResolver) {
        final int permissionFlags = data.getFlags()
                & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && data.getData() != null) {
            contentResolver.takePersistableUriPermission(data.getData(), permissionFlags);
        }
    }
}
