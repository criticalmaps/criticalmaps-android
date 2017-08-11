package de.stephanlindauer.criticalmaps.events;

public class GpsStatusChangedEvent {
    public enum Status {
        NONEXISTENT, OFF, LOW_ACCURACY, HIGH_ACCURACY
    }

    public Status status;
}
