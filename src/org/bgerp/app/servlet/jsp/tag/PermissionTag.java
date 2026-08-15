package org.bgerp.app.servlet.jsp.tag;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.jstl.core.ConditionalTagSupport;

import org.bgerp.app.servlet.filter.AuthFilter;
import org.bgerp.util.Log;

import ru.bgcrm.model.user.User;

/**
 * JSP tag, used also as static object for checking action's allowance
 *
 * @author Shamil Vakhitov
 */
public class PermissionTag extends ConditionalTagSupport {
    private static final Log log = Log.getLog();

    private String action;

    public PermissionTag() {
        super();
        init();
    }

    public void release() {
        super.release();
        init();
    }

    protected boolean condition() {
        User user = AuthFilter.getUser((HttpServletRequest) pageContext.getRequest());
        return user.checkPerm(action);
    }

    public void setAction(String value) {
        if (!value.contains(":")) {
            log.warn("Not complete action identifier was used: '{}'", value);
            value = value + ":null";
        }
        action = value;
    }

    private void init() {
        action = null;
    }
}