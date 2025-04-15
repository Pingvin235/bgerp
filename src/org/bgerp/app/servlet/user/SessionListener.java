package org.bgerp.app.servlet.user;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.bgerp.model.file.tmp.SessionTemporaryFiles;
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

        SessionTemporaryFiles.deleteFiles(se.getSession());
    }
}
