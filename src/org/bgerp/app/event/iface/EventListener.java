package org.bgerp.app.event.iface;

import ru.bgcrm.util.sql.ConnectionSet;

/**
 * Event listener.
 * The interface is temporary inherited from a deprecated one for backward compatibility reasons.
 * Later that parent will be removed.
 *
 * @author Shamil Vakhitov
 */
public interface EventListener<T extends Event> extends ru.bgcrm.event.listener.EventListener<T> {
    public void notify(T e, ConnectionSet conSet) throws Exception;
}
