package org.bgerp.util.log;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpSession;

import org.apache.log4j.WriterAppender;

/**
 * In-memory log appender for the current user' session.
 *
 * @author Shamil Vakhitov
 */
public class SessionLogAppender extends WriterAppender {
    public static final String TRACKING_KEY = SessionLogAppender.class.getName();
    static final int MAX_LOG_LINES = 1000;
    /** key - thread, value - tracked session */
    private static final Map<Thread, TrackedSession> TRACKED_SESSIONS = new ConcurrentHashMap<>();

    public SessionLogAppender() {
        setWriter(new SessionBufferWriter());
    }

    /**
     * Add to tracked the current thread for a session
     * @param session the session
     */
    public static final void trackSession(HttpSession session, boolean create) {
        TrackedSession tracked = getTrackedSession(session);
        if (tracked == null && create)
            tracked = new TrackedSession(session);
        if (tracked != null)
            TRACKED_SESSIONS.put(Thread.currentThread(), tracked);
    }

    /**
     * Removes references of all threads to a tracked session
     * @param session the session
     */
    public static final void untrackSession(HttpSession session) {
        TRACKED_SESSIONS.values().removeIf(tracked -> tracked.session == session);
    }

    /**
     * Found a tracked session for the current thread
     * @return the found tracked session or {@code null}
     */
    public static TrackedSession getTrackedSession() {
        return TRACKED_SESSIONS.get(Thread.currentThread());
    }

    public static boolean isSessionTracked(HttpSession session) {
        return getTrackedSession(session) != null;
    }

    private static TrackedSession getTrackedSession(HttpSession session) {
        return TRACKED_SESSIONS.values().stream()
                .filter(ts -> ts.session == session)
                .findAny().orElse(null);
    }

    public static final String getSessionLog(HttpSession session) {
        StringBuilder result = new StringBuilder(MAX_LOG_LINES * 200);

        TrackedSession tracked = TRACKED_SESSIONS.values().stream()
                .filter(ts -> ts.session == session)
                .findAny().orElse(null);
        if (tracked != null) {
            for (Object line : tracked.buffer)
                result.append((char[]) line);
        }

        return result.toString();
    }

    private class SessionBufferWriter extends Writer {
        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            TrackedSession tracked = TRACKED_SESSIONS.get(Thread.currentThread());
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
