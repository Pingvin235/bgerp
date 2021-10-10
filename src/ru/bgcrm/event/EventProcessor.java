package ru.bgcrm.event;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.bgerp.util.Log;

import ru.bgcrm.dao.EventProcessorLogDAO;
import ru.bgcrm.dynamic.DynamicClassManager;
import ru.bgcrm.event.listener.DynamicEventListener;
import ru.bgcrm.event.listener.EventListener;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.model.EventProcessorLogEntry;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;
import ru.bgcrm.util.sql.SQLUtils;

public class EventProcessor {
    private static final Log log = Log.getLog();

    private static Object sync = new Object();
    private static Map<Class<?>, List<EventListener<?>>> subscribers = new ConcurrentHashMap<Class<?>, List<EventListener<?>>>();

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
        List<EventListener<?>> listeners;
        synchronized (sync) {
            listeners = subscribers.get(clazz);
            if (listeners == null) {
                listeners = new CopyOnWriteArrayList<EventListener<?>>();
                subscribers.put(clazz, listeners);
            }
        }

        listeners.add(l);
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

    public static void subscribeDynamicClasses() {
        Setup setup = Setup.getSetup();
        for (String className : Utils.toList(setup.get("createOnStart"))) {
            log.info("Create class on start: " + className);

            try {
                unsubscribe(className);
                DynamicClassManager.getClass(className).getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
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
        log.debug("Processing event: %s, className: %s", event, className);

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
        return java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments().toString()
                .indexOf("jdwp") >= 0;
    }

    private static void processingEvent(Event event, EventListener<Event> listener, ConnectionSet conSet)
            throws Exception {
        final Setup setup = Setup.getSetup();
        final var key = "event.process.timeout";
        final long timeout = setup.getSokLong(5000L, key, "event.processTimeout", "dynamicEventListenerTimeOut");

        String resultStatus = "";
        long timeStart = Calendar.getInstance().getTimeInMillis();
        try {
            try {
                if (listener instanceof DynamicEventListener && !isDebugMode()) {
                    Future<byte[]> future = executor.submit(new RequestTask(listener, event, conSet));
                    future.get(timeout, TimeUnit.MILLISECONDS);
                } else {
                    listener.notify(event, conSet);
                }
            } catch (TimeoutException e) {
                throw new BGMessageException("Время ожидания выполнения скрипта '%s' истекло! (%s=%s мс).", listener.getClass().getName(), key, timeout);
            } catch (InterruptedException | ExecutionException e) {
                throw new BGMessageException("При выполнении скрипта '%s' возникло исключение '%s'", listener.getClass().getName(), e.getMessage());
            }

            resultStatus = "Successful";
        } finally {
            long timeEnd = Calendar.getInstance().getTimeInMillis();

            // TODO: Extract to some plugin. DBA?
            if (("Successful".equals(resultStatus) && setup.getBoolean("logSuccessfulEventsThrow", false))
                    || (!"Successful".equals(resultStatus) && setup.getBoolean("logErrorEventsThrow", false))
                    || setup.getBoolean("logAllEventsThrow", false)) {
                writeLog(event, listener, resultStatus, conSet.getConnection(), timeEnd - timeStart);
            }
        }
    }

    private static void writeLog(Event event, EventListener<Event> listener, String resultStatus,
            Connection eventConnection, long duration) throws BGMessageException {
        Connection connection = null;
        try {
            connection = Setup.getSetup().getDBConnectionFromPool();

            EventProcessorLogEntry logEntry = new EventProcessorLogEntry();
            logEntry.setConnectionId(SQLUtils.getConnectionId(eventConnection));
            logEntry.setInstanceHostName(System.getProperty("user.name"));
            logEntry.setEvent(event.getClass().getName());
            logEntry.setScript(listener.getClass().getName());

            int logEntryId = new EventProcessorLogDAO(connection).insertLogEntry(logEntry);

            EventProcessorLogDAO eventProcessorLogDAO = new EventProcessorLogDAO(connection);
            eventProcessorLogDAO.updateLogEntryDuration(logEntryId, duration);
            eventProcessorLogDAO.updateLogEntryResultStatus(logEntryId, resultStatus);

            connection.commit();
        } catch (SQLException e) {
            throw new BGMessageException(e.getMessage());
        } finally {
            SQLUtils.closeConnection(connection);
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
