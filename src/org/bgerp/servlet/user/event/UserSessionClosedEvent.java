package org.bgerp.servlet.user.event;

import org.bgerp.servlet.user.UserSession;

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
