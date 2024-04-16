package org.bgerp.app.event;

import org.bgerp.app.event.iface.Event;
import org.bgerp.app.event.iface.EventListener;

record PrioritizedListener(EventListener<Event> listener, int priority) {
}
