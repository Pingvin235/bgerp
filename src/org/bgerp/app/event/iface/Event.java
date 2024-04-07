package org.bgerp.app.event.iface;

/**
 * App's event, the interface is temporary inherited from a deprecated one for backward compatibility reasons.
 * Later that extending will be removed.
 *
 * @author Shamil Vakhitov
 */
public interface Event extends ru.bgcrm.event.Event {
    public static final String KEY = "event";
}
