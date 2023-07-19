package org.bgerp.app.servlet.user;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.bgerp.app.cfg.Setup;
import org.bgerp.app.servlet.user.event.UserSessionClosedEvent;
import org.bgerp.app.servlet.user.event.UserSessionCreatedEvent;
import org.bgerp.util.Log;

import ru.bgcrm.cache.UserCache;
import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.SetupChangedEvent;
import ru.bgcrm.event.listener.EventListener;
import ru.bgcrm.model.user.User;
import ru.bgcrm.util.Utils;

/**
 * List of logged in user sessions.
 *
 * @author Shamil Vakhitov
 */
public class LoginStat {
    private static final Log log = Log.getLog();

    private static LoginStat instance;

    public static LoginStat getLoginStat() {
        if (instance == null) {
            instance = new LoginStat();
        }

        return instance;
    }

    // end of static part
    private volatile long sessionTimeout = 0;

    /**
     * Key - session ID.
     */
    private final Map<String, UserSession> sessionMap = Collections.synchronizedMap(new LinkedHashMap<>());

    /**
     * Logged users IDs ordered by first session login time.
     */
    private volatile List<Integer> loggedUserIds = List.of();

    private LoginStat() {
        try {
            EventListener<SetupChangedEvent> changeListener = (e, conSet) -> {
                sessionTimeout = Setup.getSetup().getSokLong(0L, "user.session.timeout", "sessionTimeout") * 1000L;
                log.debug("sessionTimeout: {}", sessionTimeout);
            };

            changeListener.notify(null, null);

            EventProcessor.subscribe(changeListener, SetupChangedEvent.class);
        } catch (Exception e) {
            log.error(e);
        }
    }

    /**
     * Registers user session after auth.
     * @param session HTTP session.
     * @param user user.
     * @param ip IP address.
     */
    public void userLoggedIn(HttpSession session, User user, String ip) {
        synchronized (sessionMap) {
            if (session != null && user != null) {
                UserSession userSession = new UserSession(user, ip);

                sessionMap.put(session.getId(), userSession);
                loggedUserIds = updateUserLoggedList();

                try {
                    EventProcessor.processEvent(new UserSessionCreatedEvent(userSession), null);
                } catch (Exception e) {
                    log.error(e);
                }

                log.debug("User logged: {}; userList size: {}; session: {}", user, loggedUserIds.size(), session.getId());
            }
        }
    }

    /**
     * Unregister user session.
     * @param session HTTP session.
     */
    public void sessionClosed(HttpSession session) {
        synchronized (sessionMap) {
            if (sessionMap != null && session != null) {
                UserSession userSession = sessionMap.remove(session.getId());
                loggedUserIds = updateUserLoggedList();

                if (userSession != null) {
                    try {
                        EventProcessor.processEvent(new UserSessionClosedEvent(userSession), null);
                    } catch (Exception e) {
                        log.error(e);
                    }
                    log.debug("User session closed: {}; userList size: {}", userSession.user, loggedUserIds.size());
                }
            }
        }
    }

    private List<Integer> updateUserLoggedList() {
        Set<Integer> result = new LinkedHashSet<>();

        for (UserSession data : sessionMap.values()) {
            result.add(data.user.getId());
        }

        return new ArrayList<Integer>(result);
    }

    /**
     * Updates session last activity time.
     * @param session HTTP session.
     */
    public void actionWasCalled(HttpSession session) {
        UserSession data = sessionMap.get(session.getId());
        if (data != null) {
            data.lastActive = System.currentTimeMillis();
        }
    }

    /**
     * Checks if session is not timed out.
     * @param session HTTP session.
     * @return last activity time is not older as timeout.
     */
    public boolean isSessionValid(HttpSession session) {
        if (sessionTimeout > 0) {
            UserSession data = sessionMap.get(session.getId());
            if (data != null && (data.lastActive + sessionTimeout < System.currentTimeMillis())) {
                log.debug("User session invalidated by timeout: {}; userList size: {}", data.user, loggedUserIds.size());
                return false;
            }
        }

        return true;
    }

    /**
     * Logged users ordered by first session login time.
     * @return
     */
    public List<User> getLoggedUserList() {
        return Utils.getObjectList(UserCache.getUserMap(), loggedUserIds);
    }

    public LinkedHashMap<User, List<UserSession>> getLoggedUserWithSessions() {
        LinkedHashMap<User, List<UserSession>> result = new LinkedHashMap<User, List<UserSession>>();

        for (UserSession data : sessionMap.values()) {
            User user = data.user;

            List<UserSession> userSessions = result.get(user);
            if (userSessions == null) {
                result.put(user, userSessions = new ArrayList<UserSession>());
            }

            userSessions.add(data);
        }

        return result;
    }
}
