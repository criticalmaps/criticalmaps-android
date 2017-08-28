package de.stephanlindauer.criticalmaps.events;

public final class Events {
    public final static NewLocationEvent NEW_LOCATION_EVENT = new NewLocationEvent();
    public final static NewServerResponseEvent NEW_SERVER_RESPONSE_EVENT = new NewServerResponseEvent();
    public final static NetworkConnectivityChangedEvent NETWORK_CONNECTIVITY_CHANGED_EVENT = new NetworkConnectivityChangedEvent();
    public final static GpsStatusChangedEvent GPS_STATUS_CHANGED_EVENT = new GpsStatusChangedEvent();
}
