package org.bgerp.app.event;

import java.util.concurrent.Callable;

import org.bgerp.app.event.iface.Event;
import org.bgerp.app.event.iface.EventListener;
import org.bgerp.util.log.SessionLogAppender;
import org.bgerp.util.log.TrackedSession;

import ru.bgcrm.util.sql.ConnectionSet;

class RequestTask implements Callable<byte[]> {
    private final ConnectionSet conSet;
    private final Event event;
    private final EventListener<Event> listener;
    /** If the log tracked session is not null, it has to be tracked for the running thread as well */
    private final TrackedSession trackedSession;

    public RequestTask(ConnectionSet conSet, Event event, EventListener<Event> listener) {
        this.conSet = conSet;
        this.event = event;
        this.listener = listener;
        this.trackedSession = SessionLogAppender.getTracked();
    }

    @Override
    public byte[] call() throws Exception {
        try {
            if (trackedSession != null)
                SessionLogAppender.track(trackedSession.getSession(), false);
            listener.notify(event, conSet);
        } finally {
            if (trackedSession != null)
                SessionLogAppender.untrack();
        }

        return new byte[0];
    }
}