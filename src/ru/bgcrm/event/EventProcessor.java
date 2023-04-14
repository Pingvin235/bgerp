package ru.bgcrm.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.bgerp.util.Log;

import ru.bgcrm.dynamic.DynamicClassManager;
import ru.bgcrm.event.listener.EventListener;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

public class EventProcessor {
    private static final Log log = Log.getLog();

    private static final Map<Class<?>, List<EventListener<?>>> subscribers = new ConcurrentHashMap<>();

    private static class NamedThreadFactory implements ThreadFactory {
        private static ThreadFactory defaultThreadFactory = Executors.defaultThreadFactory();

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = defaultThreadFactory.newThread(r);
            thread.setName("EventProcessor-" + thread.getName());
            return thread;
        }
    }

    private static final ExecutorService executor = Executors.newCachedThreadPool(new NamedThreadFactory());

    /**
     * Подписывает слушателя на события определённого класса.
     * Слушатель должен быть отписан, если он больше не должен получать событие.
     * @param l
     * @param clazz
     */
    public static <E extends Event> void subscribe(EventListener<? super E> l, Class<E> clazz) {
        subscribers
            .computeIfAbsent(clazz, c -> new ArrayList<>())
            .add(l);
    }

    /**
     * Отписывает слушателя ото всех событий.
     * @param l
     */
    public static void unsubscribe(EventListener<?> l) {
        subscribers.values().remove((Object) l);
    }

    /**
     * Отписывает слушателя по имени его класса ото всех событий.
     * @param listenerClassName
     */
    public static void unsubscribe(String listenerClassName) {
        for (List<EventListener<?>> listenerList : subscribers.values()) {
            for (EventListener<?> listener : listenerList) {
                if (listener.getClass().getName().equals(listenerClassName)) {
                    listenerList.remove(listener);
                }
            }
        }
    }

    /**
     * Обрабатывает событие только системными обработчиками.
     *
     * @param e
     * @param connectionSet
     * @throws BGMessageException
     */
    public static void processEvent(Event e, ConnectionSet connectionSet) throws Exception {
        processEvent(e, null, connectionSet);
    }

    /**
     * Обрабатывает событие системными обработчиками а затем классом, если указан.
     * @param event
     * @param className
     * @param conSet
     * @param systemListenerProcessing
     *
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static boolean processEvent(Event event, String className, ConnectionSet conSet,
            boolean systemListenerProcessing) throws Exception {
        log.trace("Processing event: {}, className: {}", event, className);

        if (systemListenerProcessing) {
            // обработка системными зарегестрированными слушателями
            List<EventListener<?>> listeners = subscribers.get(event.getClass());
            if (listeners != null) {
                for (EventListener l : listeners) {
                    processingEvent(event, l, conSet);
                }
            }
        }

        // обработка объявленным классом-обработчиком (если есть)
        if (Utils.notBlankString(className)) {
            //TODO: Сделать алармы.
            EventListener<Event> listener = null;

            try {
                listener = DynamicClassManager.newInstance(className);
            } catch (ClassNotFoundException e) {
                log.error("Class not found: " + className, e);
            } catch (Exception e) {
                throw new BGException(e.getMessage(), e);
            }

            if (listener == null) {
                log.error("Not found class: " + className);
            } else {
                processingEvent(event, listener, conSet);
                return true;
            }
        }

        return false;
    }

    private static boolean isDebugMode() {
        return java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments().toString().indexOf("jdwp") >= 0;
    }

    private static void processingEvent(Event event, EventListener<Event> listener, ConnectionSet conSet) throws Exception {
        final Setup setup = Setup.getSetup();
        final var key = "event.process.timeout";
        final long timeout = setup.getSokLong(5000L, key, "event.processTimeout", "dynamicEventListenerTimeOut");

        try {
            if (!isDebugMode()) {
                Future<byte[]> future = executor.submit(new RequestTask(listener, event, conSet));
                future.get(timeout, TimeUnit.MILLISECONDS);
            } else {
                listener.notify(event, conSet);
            }
        } catch (TimeoutException e) {
            log.error("Timeout {} ms was exceeded in listener {}", timeout, listener.getClass().getName());
        } catch (InterruptedException | ExecutionException e) {
            log.error("In listener {} occurred an exception: {}", listener.getClass().getName(), e.getMessage());
            log.error(e);
        }
    }

    /**
     * Обрабатывает событие системными обработчиками а затем классом, если указан.
     * @param event
     * @param className
     * @param conSet
     *
     * @return
     */
    public static boolean processEvent(Event event, String className, ConnectionSet conSet) throws Exception {
        return processEvent(event, className, conSet, true);
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
