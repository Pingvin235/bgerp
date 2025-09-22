package org.bgerp.util.log;

import javax.servlet.http.HttpSession;

import org.apache.commons.collections4.queue.CircularFifoQueue;

public class TrackedSession {
    static final int MAX_LOG_LINES = 1000;

    final HttpSession session;
    final CircularFifoQueue<char[]> buffer = new CircularFifoQueue<>(MAX_LOG_LINES);

    TrackedSession(HttpSession session) {
        this.session = session;
    }

    public HttpSession getSession() {
        return session;
    }
}