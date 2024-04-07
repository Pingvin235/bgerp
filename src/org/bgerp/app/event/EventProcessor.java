package org.bgerp.app.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.bgerp.app.cfg.Setup;
import org.bgerp.app.event.iface.Event;
import org.bgerp.app.event.iface.EventListener;
import org.bgerp.app.exception.BGMessageException;
import org.bgerp.util.Log;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import ru.bgcrm.util.sql.ConnectionSet;

/**
 * Event processor, implementing Singleton and Observable patterns.
 *
 * @author Shamil Vakhitov
 */
public class EventProcessor {
    private static final Log log = Log.getLog();

    private static final Map<Class<?>, List<EventListener<?>>> SUBSCRIBERS = new ConcurrentHashMap<>();
    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("EventProcessor-%d").build());

    /**
     * Subscribes a listener to events of defined classes.
     * @param l the listener.
     * @param clazz the event class.
     */
    public static <E extends Event> void subscribe(EventListener<? super E> l, Class<E> clazz) {
        SUBSCRIBERS
            .computeIfAbsent(clazz, c -> new ArrayList<>())
            .add(l);
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
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static void processEvent(Event event, ConnectionSet conSet) throws Exception {
        log.trace("Processing event: {}", event);

        List<EventListener<?>> listeners = SUBSCRIBERS.get(event.getClass());
        if (listeners != null)
            for (EventListener l : listeners)
                processEvent(event, l, conSet);
    }

    private static void processEvent(Event event, EventListener<Event> listener, ConnectionSet conSet) throws Exception {
        final long timeout = Setup.getSetup().getLong("event.process.timeout", 5000L);
        try {
            if (!isDebugMode()) {
                Future<byte[]> future = EXECUTOR.submit(new RequestTask(listener, event, conSet));
                future.get(timeout, TimeUnit.MILLISECONDS);
            } else {
                listener.notify(event, conSet);
            }
        } catch (TimeoutException e) {
            log.error("Timeout {} ms was exceeded in listener {}", timeout, listener.getClass().getName());
        } catch (InterruptedException | ExecutionException e) {
            var cause = e.getCause();
            if (!(cause instanceof BGMessageException)) {
                log.error("In listener {} occurred an exception: {}", listener.getClass().getName(), e.getMessage());
                log.error(e);
            }
            throw (Exception) cause;
        }
    }

    private static boolean isDebugMode() {
        return java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments().toString().indexOf("jdwp") >= 0;
    }

    private static class RequestTask implements Callable<byte[]> {
        private ConnectionSet conSet;
        private Event event;
        private EventListener<Event> listener;

        public RequestTask(EventListener<Event> listener, Event event, ConnectionSet conSet) {
            this.listener = listener;
            this.event = event;
            this.conSet = conSet;
        }

        @Override
        public byte[] call() throws Exception {
            listener.notify(event, conSet);
            return new byte[0];
        }
    }
}
