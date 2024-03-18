package org.bgerp.app.servlet.user;

import java.util.Date;

import javax.servlet.http.HttpSession;

import ru.bgcrm.model.user.User;

/**
 * User session data.
 *
 * @author Shamil Vakhitov
 */
public class UserSession {
    private final HttpSession session;
    final User user;
    private final String ip;
    private final Date loginTime = new Date();
    /** Last activity time except pulling. */
    long lastActive = loginTime.getTime();

    UserSession(HttpSession session, User user, String ip) {
        this.session = session;
        this.user = user;
        this.ip = ip;
    }

    public HttpSession getSession() {
        return session;
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

    public long getLastActive() {
        return lastActive;
    }
}