package ru.bgcrm.servlet;

import java.io.File;
import java.sql.Connection;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.log4j.Logger;

import ru.bgcrm.cache.UserCache;
import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.authentication.UserSessionClosedEvent;
import ru.bgcrm.model.user.User;
import ru.bgcrm.servlet.filter.AuthFilter;
import ru.bgcrm.struts.action.FileAction.SessionTemporaryFiles;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.sql.SQLUtils;
import ru.bgcrm.util.sql.SingleConnectionConnectionSet;

public class SessionListener implements HttpSessionListener {
	private static final Logger log = Logger.getLogger(SessionListener.class);

	public void sessionCreated(HttpSessionEvent se) {
	}

	public void sessionDestroyed(HttpSessionEvent se) {
		LoginStat.getLoginStat().sessionClosed(se.getSession());

		SessionTemporaryFiles files = (SessionTemporaryFiles) se.getSession()
				.getAttribute(SessionTemporaryFiles.STORE_KEY);
		if (files != null) {
			for (Integer fileId : files.fileTitleMap.keySet()) {
				String path = SessionTemporaryFiles.getStoreFilePath(se.getSession(), fileId);
				new File(path).delete();
			}
		}

		Integer userId = (Integer) se.getSession().getAttribute(AuthFilter.REQUEST_ATTRIBUTE_USER_ID_NAME);
		if (userId != null) {
			User user = UserCache.getUser(userId);
			Connection con = Setup.getSetup().getDBConnectionFromPool();
			try {
				EventProcessor.processEvent(new UserSessionClosedEvent(user, se),
						new SingleConnectionConnectionSet(con));
			} catch (Exception ex) {
				log.error(ex.getMessage(), ex);
			} finally {
				SQLUtils.closeConnection(con);
			}
		} else {
			log.warn("User not found in closed session: " + userId);
		}
	}
}
