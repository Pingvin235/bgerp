package org.bgerp.app.event.iface;

import ru.bgcrm.util.sql.ConnectionSet;

/**
 * Event listener.
 *
 * @author Shamil Vakhitov
 */
public interface EventListener<T extends Event> {
    public void notify(T e, ConnectionSet conSet) throws Exception;
}
