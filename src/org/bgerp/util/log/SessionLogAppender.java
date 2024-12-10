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
    /** key - thread, value - tracked session */
    private static final Map<Thread, TrackedSession> TRACKED = new ConcurrentHashMap<>();

    public SessionLogAppender() {
        setWriter(new SessionBufferWriter());
    }

    /**
     * Add to tracked the current thread for a session
     * @param session the session
     */
    public static final void track(HttpSession session, boolean create) {
        TrackedSession tracked = getTracked(session);
        if (tracked == null && create)
            tracked = new TrackedSession(session);
        if (tracked != null)
            TRACKED.put(Thread.currentThread(), tracked);
    }

    /**
     * Removes the current thread mapping to a tracked session
     */
    public static final void untrack() {
        TRACKED.remove(Thread.currentThread());
    }

    /**
     * Removes references of all threads to a tracked session
     * @param session the session
     */
    public static final void untrack(HttpSession session) {
        TRACKED.values().removeIf(tracked -> tracked.session == session);
    }

    /**
     * Found a tracked session for the current thread
     * @return the found tracked session or {@code null}
     */
    public static TrackedSession getTracked() {
        return TRACKED.get(Thread.currentThread());
    }

    public static boolean isTracked(HttpSession session) {
        return getTracked(session) != null;
    }

    private static TrackedSession getTracked(HttpSession session) {
        return TRACKED.values().stream()
                .filter(ts -> ts.session == session)
                .findAny().orElse(null);
    }

    public static final String getSessionLog(HttpSession session) {
        StringBuilder result = new StringBuilder(TrackedSession.MAX_LOG_LINES * 200);

        TrackedSession tracked = TRACKED.values().stream()
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
            TrackedSession tracked = TRACKED.get(Thread.currentThread());
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
