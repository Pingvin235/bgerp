package org.bgerp.util.log;

import javax.servlet.http.HttpSession;

import org.apache.commons.collections.buffer.CircularFifoBuffer;

public class TrackedSession {
    static final int MAX_LOG_LINES = 1000;

    final HttpSession session;
    final CircularFifoBuffer buffer = new CircularFifoBuffer(MAX_LOG_LINES);

    TrackedSession(HttpSession session) {
        this.session = session;
    }

    public HttpSession getSession() {
        return session;
    }
}