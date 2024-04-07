package ru.bgcrm.event.listener;

import ru.bgcrm.event.Event;
import ru.bgcrm.util.sql.ConnectionSet;

/**
 * Use {@link org.bgerp.app.event.iface.EventListener} instead.
 */
@Deprecated
public interface EventListener<T extends Event> {
    public void notify(T e, ConnectionSet conSet) throws Exception;
}
