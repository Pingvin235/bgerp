package org.bgerp.util.log;

import javax.servlet.http.HttpSession;

import org.apache.commons.collections.buffer.CircularFifoBuffer;

public class TrackedSession {
    final HttpSession session;
    final CircularFifoBuffer buffer = new CircularFifoBuffer(SessionLogAppender.MAX_LOG_LINES);

    TrackedSession(HttpSession session) {
        this.session = session;
    }

    public HttpSession getSession() {
        return session;
    }
}