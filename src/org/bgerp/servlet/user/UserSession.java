package org.bgerp.servlet.user;

import java.util.Date;

import ru.bgcrm.model.user.User;

/**
 * User session data.
 *
 * @author Shamil Vakhitov
 */
public class UserSession {
    final User user;
    private final String ip;
    private final Date loginTime = new Date();
    /** Last activity time except pulling. */
    long lastActive = loginTime.getTime();

    UserSession(User user, String ip) {
        this.user = user;
        this.ip = ip;
    }

    public User getUser() {
        return user;
    }

    public String getIp() {
        return ip;
    }

    public Date getLoginTime() {
        return loginTime;
    }

    public Date getLastActiveTime() {
        return new Date(lastActive);
    }
}