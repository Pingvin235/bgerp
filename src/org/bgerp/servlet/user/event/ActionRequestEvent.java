package org.bgerp.servlet.user.event;

import javax.servlet.http.HttpServletRequest;

import ru.bgcrm.event.Event;
import ru.bgcrm.model.user.PermissionNode;

/**
 * HTTP request calling action.
 *
 * @author Shamil Vakhitov
 */
public class ActionRequestEvent implements Event {
    private final HttpServletRequest request;
    private final String action;
    private final PermissionNode permissionNode;
    private final long duration;
    private final String error;

    public ActionRequestEvent(HttpServletRequest request, String action, PermissionNode permissionNode, long duration, String error) {
        this.request = request;
        this.action = action;
        this.permissionNode = permissionNode;
        this.duration = duration;
        this.error = error;
    }

    /**
     * @return HTTP request.
     */
    public HttpServletRequest getRequest() {
        return request;
    }

    /**
     * @return action identifier, semicolon separated action class name and called method.
     */
    public String getAction() {
        return action;
    }

    /**
     * @return action permission node.
     */
    public PermissionNode getPermissionNode() {
        return permissionNode;
    }

    /**
     * @return execution duration in milliseconds.
     */
    public long getDuration() {
        return duration;
    }

    /**
     * @return execution status string, empty when successful.
     */
    public String getError() {
        return error;
    }
}
