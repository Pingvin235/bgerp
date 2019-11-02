package ru.bgcrm.servlet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import ru.bgcrm.cache.UserCache;
import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.SetupChangedEvent;
import ru.bgcrm.event.listener.EventListener;
import ru.bgcrm.model.user.User;
import ru.bgcrm.servlet.jsp.JSPFunction;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.sql.ConnectionSet;

public class LoginStat {
	private static final Logger log = Logger.getLogger(LoginStat.class);
	private static LoginStat loginStat;

	public static LoginStat getLoginStat() {
		if (loginStat == null) {
			loginStat = new LoginStat();
		}

		return loginStat;
	}

	public static class SessionData {
		private final User user;
		private final Date loginTime = new Date();
		// время последней активности без учёта пуллинга
		private long lastActive = loginTime.getTime();

		private SessionData(User user) {
			this.user = user;
		}

		public User getUser() {
			return user;
		}

		public Date getLoginTime() {
			return loginTime;
		}

		public Date getLastActiveTime() {
			return new Date(lastActive);
		}
	}

	//конец статической части
	private volatile long sessionTimeout = 0;

	// мап сессий с ключом - идентификатором сессии
	private final Map<String, SessionData> sessionMap = Collections
			.synchronizedMap(new LinkedHashMap<String, SessionData>());
	// лист кодов авторизовавшихся пользователей в порядке авторизации
	private volatile List<Integer> loggedUserIds = null;

	private LoginStat() {
		try {
			EventListener<SetupChangedEvent> changeListener = new EventListener<SetupChangedEvent>() {
				@Override
				public void notify(SetupChangedEvent e, ConnectionSet conSet) throws Exception {
					sessionTimeout = Setup.getSetup().getLong("sessionTimeout", 0) * 1000L;

					if (log.isDebugEnabled()) {
						log.debug("sessionTimeout: " + sessionTimeout);
					}
				}
			};

			changeListener.notify(null, null);

			EventProcessor.subscribe(changeListener, SetupChangedEvent.class);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void userLoggedIn(HttpSession session, User user) {
		synchronized (sessionMap) {
			if (session != null && user != null) {
				sessionMap.put(session.getId(), new SessionData(user));
				loggedUserIds = updateUserLoggedList();

				if (log.isDebugEnabled()) {
					log.debug("User logged: " + user + "; userList size: " + loggedUserIds.size() + "; session: "
							+ session.getId());
				}
			}
		}
	}

	public void sessionClosed(HttpSession session) {
		synchronized (sessionMap) {
			if (sessionMap != null && session != null) {
				SessionData data = sessionMap.remove(session.getId());
				loggedUserIds = updateUserLoggedList();

				if (data != null && log.isDebugEnabled()) {
					log.debug("User session closed: " + data.user + "; userList size: " + loggedUserIds.size());
				}
			}
		}
	}

	private List<Integer> updateUserLoggedList() {
		Set<Integer> result = new LinkedHashSet<Integer>();

		for (SessionData data : sessionMap.values()) {
			result.add(data.user.getId());
		}

		return new ArrayList<Integer>(result);
	}

	public void actionWasCalled(HttpSession session) {
		SessionData data = sessionMap.get(session.getId());
		if (data != null) {
			data.lastActive = System.currentTimeMillis();
		}
	}

	public boolean isSessionValid(HttpSession session) {
		if (sessionTimeout > 0) {
			SessionData data = sessionMap.get(session.getId());
			if (data != null && (data.lastActive + sessionTimeout < System.currentTimeMillis())) {
				if (log.isDebugEnabled()) {
					log.debug("User session invalid by timeout: " + data.user + "; userList size: "
							+ loggedUserIds.size());
				}
				return false;
			}
		}

		return true;
	}

	/**
	 * Возвращает список авторизовавшихся пользователей в порядке первых авторизаций.
	 */
	public List<User> getLoggedUserList() {
		return JSPFunction.getObjectList(UserCache.getUserMap(), loggedUserIds);
	}

	public LinkedHashMap<User, List<SessionData>> getLoggedUserWithSessions() {
		LinkedHashMap<User, List<SessionData>> result = new LinkedHashMap<User, List<SessionData>>();

		for (SessionData data : sessionMap.values()) {
			User user = data.user;

			List<SessionData> userSessions = result.get(user);
			if (userSessions == null) {
				result.put(user, userSessions = new ArrayList<SessionData>());
			}

			userSessions.add(data);
		}

		return result;
	}
}
