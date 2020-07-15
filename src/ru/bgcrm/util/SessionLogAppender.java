package ru.bgcrm.util;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpSession;

import org.apache.commons.collections.buffer.CircularFifoBuffer;
import org.apache.log4j.WriterAppender;

/**
 * Сборщик логов сессии пользователя в память.
 */
public class SessionLogAppender extends WriterAppender {
    public static final String TRACKING_KEY = SessionLogAppender.class.getName();
    private static final int MAX_LOG_LINES = 1000;

    public SessionLogAppender() {
        setWriter(new SessionBufferWriter());
    }

    /** Ключ - поток, значение - отслеживаемая сессия. */
    private static final Map<Thread, TrackedSession> trackedSessions = new ConcurrentHashMap<>();

    /**
     * Обновляет поток для отслеживаемой сессии и регистрирует его при необходимости.
     * @param session
     */
    public static final void trackSession(HttpSession session, boolean create) {
        TrackedSession tracked = untrackSession(session);
        if (tracked == null && create)
            tracked = new TrackedSession(session);
        if (tracked != null)
            trackedSessions.put(Thread.currentThread(), tracked);
    }

    public static final TrackedSession untrackSession(HttpSession session) {
        Collection<TrackedSession> valuesIterator = trackedSessions.values();
        for (TrackedSession tracked : valuesIterator) {
            if (tracked.session == session) {
                valuesIterator.remove(tracked);
                return tracked;
            }
        }
        return null;
    }

    public static boolean isSessionTracked(HttpSession session) {
        return getTrackedSession(session) != null;
    }

    private static TrackedSession getTrackedSession(HttpSession session) {
        return trackedSessions.values().stream()
                .filter(ts -> ts.session == session)
                .findAny().orElse(null);
    }

    public static final String getSessionLog(HttpSession session) {
        StringBuilder result = new StringBuilder(MAX_LOG_LINES * 200);

        TrackedSession tracked = trackedSessions.values().stream()
                .filter(ts -> ts.session == session)
                .findAny().orElse(null);
        if (tracked != null) {
            for (Object line : tracked.buffer)
                result.append((char[]) line);
        }

        return result.toString();
    }

    private static class TrackedSession {
        private final HttpSession session;
        private final CircularFifoBuffer buffer = new CircularFifoBuffer(MAX_LOG_LINES);

        private TrackedSession(HttpSession session) {
            this.session = session;
        }
    }

    private class SessionBufferWriter extends Writer {
        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            TrackedSession tracked = trackedSessions.get(Thread.currentThread());
            if (tracked != null) {
                tracked.buffer.add(Arrays.copyOfRange(cbuf, off, off + len));
            }
        }

        @Override
        public void flush() throws IOException {}

        @Override
        public void close() throws IOException {}
    }
}
