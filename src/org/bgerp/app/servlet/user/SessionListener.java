package org.bgerp.app.servlet.user;

import java.io.File;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.bgerp.model.file.SessionTemporaryFiles;
import org.bgerp.util.Dynamic;

/**
 * HTTP session listener, configured in web.xml.
 *
 * @author Shamil Vakhitov
 */
@Dynamic
public class SessionListener implements HttpSessionListener {
    public void sessionCreated(HttpSessionEvent se) {}

    public void sessionDestroyed(HttpSessionEvent se) {
        LoginStat.instance().sessionClosed(se.getSession());

        SessionTemporaryFiles files = (SessionTemporaryFiles) se.getSession().getAttribute(SessionTemporaryFiles.STORE_KEY);
        if (files != null) {
            for (Integer fileId : files.fileTitleMap.keySet()) {
                String path = SessionTemporaryFiles.getStoreFilePath(se.getSession(), fileId);
                new File(path).delete();
            }
        }
    }
}
