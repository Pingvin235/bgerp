package org.bgerp.servlet.user.event;

import org.bgerp.servlet.user.UserSession;

import ru.bgcrm.event.Event;

/**
 * User session created.
 *
 * @author Shamil Vakhitov
 */
public class UserSessionCreatedEvent implements Event {
    private final UserSession session;

    public UserSessionCreatedEvent(UserSession session) {
        this.session = session;
    }

    public UserSession getSession() {
        return session;
    }
}
