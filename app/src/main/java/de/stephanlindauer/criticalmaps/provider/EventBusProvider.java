package de.stephanlindauer.criticalmaps.provider;

import com.squareup.otto.Bus;

public class EventBusProvider {

    private final Bus bus = new Bus();

    //singleton
    private static EventBusProvider instance;

    public static EventBusProvider getInstance() {
        if (instance == null) {
            instance = new EventBusProvider();
        }
        return instance;
    }

    public void post(Object event) {
        bus.post(event);
    }

    public void register(Object object) {
        bus.register(object);
    }

    public void unregister(Object object) {
        bus.unregister(object);
    }
}
