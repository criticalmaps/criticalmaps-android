package de.stephanlindauer.criticalmaps.utils;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.Build;

public class GpxUtils {

    @SuppressLint("WrongConstant") // Flags from getFlags() are valid
    public static void persistPermissionOnFile(Intent data, ContentResolver contentResolver) {
        final int permissionFlags = data.getFlags() & Intent.FLAG_GRANT_READ_URI_PERMISSION;
        if (data.getData() != null) {
            contentResolver.takePersistableUriPermission(data.getData(), permissionFlags);
        }
    }
}
