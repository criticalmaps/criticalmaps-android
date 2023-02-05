package de.stephanlindauer.criticalmaps.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class PermissionRequest {
    private final String[] permissions;
    private final String rationale;
    private final Runnable onGrantedCallback;
    private final Runnable onDeniedCallback;
    private final Runnable onPermanentlyDeniedCallback;
    private final int requestCode;

    public PermissionRequest(@NonNull String[] permissions,
                             @NonNull String rationale,
                             @Nullable Runnable onGrantedCallback,
                             @Nullable Runnable onDeniedCallback,
                             @Nullable Runnable onPermanentlyDeniedCallback) {
        this.permissions = permissions;
        this.rationale = rationale;
        this.onGrantedCallback =
                onGrantedCallback != null ? onGrantedCallback : () -> {
                };
        this.onDeniedCallback =
                onDeniedCallback != null ? onDeniedCallback : () -> {
                };
        this.onPermanentlyDeniedCallback =
                onPermanentlyDeniedCallback != null ? onPermanentlyDeniedCallback : () -> {
                };
        // Can only use lower 16 bits for requestCode --> short
        this.requestCode = (short) Math.round(Math.random() * Short.MAX_VALUE);
    }

    @NonNull
    public String[] getPermissions() {
        return permissions;
    }

    public int getRequestCode() {
        return requestCode;
    }

    @NonNull
    public Runnable getOnGrantedCallback() {
        return onGrantedCallback;
    }

    @NonNull
    public Runnable getOnDeniedCallback() {
        return onDeniedCallback;
    }

    @NonNull
    public Runnable getOnPermanentlyDeniedCallback() {
        return onPermanentlyDeniedCallback;
    }

    @NonNull
    public String getRationale() {
        return rationale;
    }
}
