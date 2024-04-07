package ru.bgcrm.event.listener;

import org.bgerp.app.event.iface.Event;
import org.bgerp.app.event.iface.EventListener;

import ru.bgcrm.util.sql.ConnectionSet;

/**
 * Use {@code implements EventListener<Event>} instead.
 */
@Deprecated
public abstract class DynamicEventListener implements EventListener<Event> {
    public abstract void notify(Event e, ConnectionSet connectionSet) throws Exception;
}
