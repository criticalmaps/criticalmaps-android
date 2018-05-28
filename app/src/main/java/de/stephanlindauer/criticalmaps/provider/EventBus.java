package de.stephanlindauer.criticalmaps.provider;

import com.squareup.otto.Bus;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class EventBus {

    private final Bus bus = new Bus();

    @Inject
    public EventBus() {
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
