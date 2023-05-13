package org.bgerp.app.servlet.user.event;

import org.bgerp.app.servlet.user.UserSession;

/**
 * User session closed.
 *
 * @author Shamil Vakhitov
 */
public class UserSessionClosedEvent extends UserSessionCreatedEvent {
    public UserSessionClosedEvent(UserSession session) {
        super(session);
    }
}
