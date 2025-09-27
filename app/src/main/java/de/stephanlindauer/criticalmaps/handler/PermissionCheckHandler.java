package de.stephanlindauer.criticalmaps.handler;

import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Arrays;

import javax.inject.Inject;
import javax.inject.Singleton;

import de.stephanlindauer.criticalmaps.App;
import de.stephanlindauer.criticalmaps.R;
import de.stephanlindauer.criticalmaps.model.PermissionRequest;
import timber.log.Timber;

@Singleton
public class PermissionCheckHandler {
    private Activity activity;
    private PermissionRequest activePermissionRequest;

    @Inject
    @SuppressWarnings("WeakerAccess")
    public PermissionCheckHandler() {
    }

    public void attachActivity(Activity activity) {
        Timber.d("Activity attached");
        this.activity = activity;
    }

    public void detachActivity() {
        Timber.d("Activity detached");
        this.activity = null;
    }

    public void requestPermissionsWithRationaleIfNeeded(PermissionRequest permissionRequest) {
        Timber.d("%s", Arrays.toString(permissionRequest.getPermissions()));
        activePermissionRequest = permissionRequest;

        // short-circuit here if already granted
        if (checkAllPermissionsGranted(permissionRequest.getPermissions())) {
            activePermissionRequest.getOnGrantedCallback().run();
            activePermissionRequest = null;
            return;
        }

        // while in background handle requests like denials
        if (activity == null) {
            activePermissionRequest.getOnDeniedCallback().run();
            activePermissionRequest = null;
            return;
        }

        boolean shouldShowRationale = false;
        for (String permission : activePermissionRequest.getPermissions()) {
            shouldShowRationale = shouldShowRationale ||
                    ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
        }
        if (shouldShowRationale) {
            requestWithRationale();
        } else {
            request();
        }
    }

    private void requestWithRationale() {
        new AlertDialog.Builder(activity, R.style.AlertDialogTheme)
                .setTitle(R.string.permissions_rationale_title)
                .setMessage(activePermissionRequest.getRationale())
                .setNegativeButton(R.string.no, null)
                .setPositiveButton(R.string.yes, (dialog, which) -> request())
                .create()
                .show();
    }

    private void request() {
        ActivityCompat.requestPermissions(
                activity,
                activePermissionRequest.getPermissions(),
                activePermissionRequest.getRequestCode());
    }

    public boolean handlePermissionRequestCallback(int requestCode, int[] grantResults) {
        Timber.d("requestCode = %s; grantResults = %s",
                requestCode, Arrays.toString(grantResults));
        if (requestCode != activePermissionRequest.getRequestCode()) {
            return false;
        }

        // Note: Can be an empty array that should be treated as cancellation.
        boolean allPermissionsGranted = grantResults.length > 0;
        for (int result : grantResults) {
            allPermissionsGranted = allPermissionsGranted &&
                    result == PackageManager.PERMISSION_GRANTED;
        }

        if (allPermissionsGranted) {
            activePermissionRequest.getOnGrantedCallback().run();
            activePermissionRequest = null;
            return true;
        } else {
            boolean shouldShowRationale = false;
            for (String permission : activePermissionRequest.getPermissions()) {
                shouldShowRationale = shouldShowRationale ||
                        ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
            }
            if (!shouldShowRationale) {
                // denied and no rationale should be shown indicates permanently denied
                activePermissionRequest.getOnPermanentlyDeniedCallback().run();
            } else {
                // user doesn't want to give permission now, but has not permanently denied
                activePermissionRequest.getOnDeniedCallback().run();
            }
            activePermissionRequest = null;
        }

        return true;
    }

    public static boolean checkAllPermissionsGranted(String[] permissions) {
        boolean permissionsGranted = true;
        for (String permission : permissions) {
            permissionsGranted = permissionsGranted && (PackageManager.PERMISSION_GRANTED ==
                    ContextCompat.checkSelfPermission(App.components().app(), permission));
        }
        Timber.d("Permissions: %s; granted: %b",
                Arrays.toString(permissions), permissionsGranted);
        return permissionsGranted;
    }
}
