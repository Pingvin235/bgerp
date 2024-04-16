package org.bgerp.app.event;

import java.util.concurrent.Callable;

import org.bgerp.app.event.iface.Event;
import org.bgerp.app.event.iface.EventListener;

import ru.bgcrm.util.sql.ConnectionSet;

class RequestTask implements Callable<byte[]> {
    private ConnectionSet conSet;
    private Event event;
    private EventListener<Event> listener;

    public RequestTask(EventListener<Event> listener, Event event, ConnectionSet conSet) {
        this.listener = listener;
        this.event = event;
        this.conSet = conSet;
    }

    @Override
    public byte[] call() throws Exception {
        listener.notify(event, conSet);
        return new byte[0];
    }
}