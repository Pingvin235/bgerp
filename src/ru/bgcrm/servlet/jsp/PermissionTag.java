package ru.bgcrm.servlet.jsp;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.jstl.core.ConditionalTagSupport;

import org.bgerp.servlet.filter.AuthFilter;
import org.bgerp.util.Log;

import ru.bgcrm.cache.UserCache;
import ru.bgcrm.model.user.PermissionNode;
import ru.bgcrm.model.user.User;

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
     * Checks if any of actions in is allowed for user.
     * @param user the user.
     * @param actions action strings in format {@code FULL_CLASS_NAME}:{@code METHOD_NAME}.
     * @return {@code true} if {@code user} has any of {@code actions} allowed.
     */
    public static boolean check(User user, String... actions) {
        for (var action : actions) {
            if (!action.contains(":")) {
                action += ":null";
            }

            if (PermissionNode.getPermissionNode(action) == null) {
                log.error("Action not found: {}", action);
                return false;
            }

            if (UserCache.getPerm(user.getId(), action) != null) {
                return true;
            }
        }
        return false;
    }
}