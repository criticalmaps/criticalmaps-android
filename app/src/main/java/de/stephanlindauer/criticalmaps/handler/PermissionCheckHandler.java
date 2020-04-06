package de.stephanlindauer.criticalmaps.handler;

import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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

    public void requestPermissionWithRationaleIfNeeded(PermissionRequest permissionRequest) {
        Timber.d("%s", permissionRequest.getPermission());
        activePermissionRequest = permissionRequest;

        // short-circuit here if already granted
        if (checkPermissionGranted(permissionRequest.getPermission())) {
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

        if (ActivityCompat.shouldShowRequestPermissionRationale(
                activity, activePermissionRequest.getPermission())) {
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
                new String[]{activePermissionRequest.getPermission()},
                activePermissionRequest.getRequestCode());
    }

    public boolean handlePermissionRequestCallback(int requestCode, int[] grantResults) {
        Timber.d("requestCode = %s", requestCode);
        if (requestCode != activePermissionRequest.getRequestCode()) {
            return false;
        }

        // we handle/need only single permission requests for now
        if (grantResults.length != 1) {
            return false;
        }

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            activePermissionRequest.getOnGrantedCallback().run();
            activePermissionRequest = null;
            return true;
        } else {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(
                    activity, activePermissionRequest.getPermission())) {
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


    public static boolean checkPermissionGranted(String permission) {
        return PackageManager.PERMISSION_GRANTED ==
                ContextCompat.checkSelfPermission(App.components().app(), permission);
    }
}
