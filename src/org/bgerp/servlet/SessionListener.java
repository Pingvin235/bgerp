package org.bgerp.servlet;

import java.io.File;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import ru.bgcrm.struts.action.FileAction.SessionTemporaryFiles;

public class SessionListener implements HttpSessionListener {
    public void sessionCreated(HttpSessionEvent se) {}

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
    }
}
