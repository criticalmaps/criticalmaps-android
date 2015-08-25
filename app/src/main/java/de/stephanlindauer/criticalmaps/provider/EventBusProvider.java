package de.stephanlindauer.criticalmaps.provider;

import com.squareup.otto.Bus;

public class EventBusProvider {

    private Bus bus = new Bus();

    //singleton
    private static EventBusProvider instance;

    public static EventBusProvider getInstance() {
        if (EventBusProvider.instance == null) {
            EventBusProvider.instance = new EventBusProvider();
        }
        return EventBusProvider.instance;
    }

    public void post(Object event) {
        bus.post(event);
    }

    public void register(java.lang.Object object) {
        bus.register(object);
    }

    public void unregister(java.lang.Object object) {
        bus.unregister(object);
    }
}
