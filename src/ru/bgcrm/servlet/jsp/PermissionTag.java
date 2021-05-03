package ru.bgcrm.servlet.jsp;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.jstl.core.ConditionalTagSupport;

import ru.bgcrm.cache.UserCache;
import ru.bgcrm.model.user.PermissionNode;
import ru.bgcrm.model.user.User;
import ru.bgcrm.servlet.filter.AuthFilter;
import ru.bgerp.util.Log;

/**
 * JSP tag, used also as static object for checking action's allowance.
 * 
 * @author Shamil Vakhitov
 */
public class PermissionTag extends ConditionalTagSupport {
    private static final Log log = Log.getLog();

    private String[] actions;

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
        return check(user, actions);
    }

    public void setAction(String allowedActions) {
        actions = allowedActions.split(",");
    }

    private void init() {
        actions = null;
    }

    /***
     * Checks if any of actions in is allowed.
     * @param user the user.
     * @param actions actions.
     * @return
     */
    public static boolean check(User user, String... actions) {
        for (var action : actions) {
            if (!action.contains(":")) {
                action += ":null";
            }

            if (PermissionNode.getPermissionNode(action) == null) {
                log.error("Action not found: %s", action);
                return false;
            }

            if (UserCache.getPerm(user.getId(), action) != null) {
                return true;
            }
        }
        return false;
    }
}