package org.bgerp.app.event.iface;

/**
 * App's event.
 * The interface is temporary inherited from a deprecated one for backward compatibility reasons.
 * Later that parent will be removed.
 *
 * @author Shamil Vakhitov
 */
public interface Event extends ru.bgcrm.event.Event {
    public static final String KEY = "event";
}
