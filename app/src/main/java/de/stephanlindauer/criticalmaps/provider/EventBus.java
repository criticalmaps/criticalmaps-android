package de.stephanlindauer.criticalmaps.provider;

import com.squareup.otto.Bus;

public class EventBus {

    private final Bus bus = new Bus();

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
