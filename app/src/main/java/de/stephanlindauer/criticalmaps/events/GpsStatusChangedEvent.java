package de.stephanlindauer.criticalmaps.events;

public final class GpsStatusChangedEvent {
    public enum Status {
        NONEXISTENT,
        DISABLED,
        PERMISSION_PERMANENTLY_DENIED,
        NO_PERMISSIONS,
        LOW_ACCURACY,
        HIGH_ACCURACY
    }

    public Status status;
}
