package de.stephanlindauer.criticalmaps.service;

import com.squareup.otto.Bus;

public class EventService {

    private Bus bus = new Bus();

    //singleton
    private static EventService instance;

    public static EventService getInstance() {
        if (EventService.instance == null) {
            EventService.instance = new EventService();
        }
        return EventService.instance;
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
