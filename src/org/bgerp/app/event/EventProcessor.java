package org.bgerp.app.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bgerp.app.event.iface.Event;
import org.bgerp.app.event.iface.EventListener;
import org.bgerp.app.exception.BGMessageException;
import org.bgerp.util.Log;

import ru.bgcrm.util.sql.ConnectionSet;

/**
 * Event processor, implementing Singleton and Observable patterns.
 *
 * @author Shamil Vakhitov
 */
public class EventProcessor {
    private static final Log log = Log.getLog();

    private static final Map<Class<?>, List<PrioritizedListener>> SUBSCRIBERS = new ConcurrentHashMap<>();

    /**
     * Subscribes a listener to events of a class.
     * @param l the listener.
     * @param clazz the event class.
     */
    public static <E extends Event> void subscribe(EventListener<? super E> l, Class<E> clazz) {
        subscribe(l, clazz, 0);
    }

    /**
     * Subscribes a listener to events of a class.
     * @param l the listener.
     * @param clazz the event class.
     * @param priority the listener's priority, high priority listeners process events earlier.
     */
    @SuppressWarnings({ "unchecked" })
    public static <E extends Event> void subscribe(EventListener<? super E> l, Class<E> clazz, int priority) {
        List<PrioritizedListener> list = SUBSCRIBERS.computeIfAbsent(clazz, c -> new ArrayList<>());

        var item = new PrioritizedListener((EventListener<Event>) l, priority);

        // insert before an item with less priority
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).priority() < priority) {
                list.add(i, item);
                return;
            }
        }
        // add listener to the end
        list.add(item);
    }

    /**
     * Unsubscribes a listener from all events.
     * @param l the listener.
     */
    public static void unsubscribe(EventListener<?> l) {
        SUBSCRIBERS.values().remove((Object) l);
    }

    /**
     * Processes an event with registered listeners.
     * @param event the event.
     * @param conSet a DB connections set.
     * @throws BGMessageException
     */
    public static void processEvent(Event event, ConnectionSet conSet) throws Exception {
        log.trace("Processing event: {}", event);

        List<PrioritizedListener> listeners = SUBSCRIBERS.get(event.getClass());
        if (listeners != null)
            for (var item : listeners) {
                if (!event.processing())
                    break;
                item.listener().notify(event, conSet);
            }
    }
}
