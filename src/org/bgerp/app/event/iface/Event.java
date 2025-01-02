package org.bgerp.app.event.iface;

/**
 * App's event
 *
 * @author Shamil Vakhitov
 */
public interface Event {
    public static final String KEY = "event";

    /**
     * @return has the event to be processed by the next listener.
     */
    public default boolean processing() {
        return true;
    }
}
