package org.bgerp.app.event.iface;

/**
 * Event listener, the interface is temporary inherited from a deprecated one for backward compatibility reasons.
 * Later that extension will be removed, everything moved to the current iface.
 *
 * @author Shamil Vakhitov
 */
public interface EventListener<T extends Event> extends ru.bgcrm.event.listener.EventListener<T> {
}
