package ru.bgcrm.event.listener;

import ru.bgcrm.event.Event;
import ru.bgcrm.util.sql.ConnectionSet;

/**
 * Use {@code implements EventListener<Event>} instead.
 */
@Deprecated
public abstract class DynamicEventListener implements EventListener<Event> {
    public abstract void notify(Event e, ConnectionSet connectionSet) throws Exception;
}
