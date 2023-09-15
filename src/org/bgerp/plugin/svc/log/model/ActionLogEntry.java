package org.bgerp.plugin.svc.log.model;

import java.util.Date;
import java.util.Map;

import org.bgerp.app.servlet.filter.AuthFilter;
import org.bgerp.app.servlet.user.event.ActionRequestEvent;
import org.bgerp.model.base.Id;

import ru.bgcrm.model.user.User;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;

/**
 * Action log record.
 *
 * @author Shamil Vakhitov
 */
public class ActionLogEntry extends Id {
    private int userId;
    private String ipAddress;
    private String action = "";
    private String parameters;
    private Date time;
    private long duration;
    private String error;

    public ActionLogEntry() {}

    public ActionLogEntry(ActionRequestEvent e) {
        var request = e.getRequest();

        StringBuilder queryString = new StringBuilder(200);
        for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
            String[] entryValue = entry.getValue();
            for (String val : entryValue) {
                queryString
                    .append(entry.getKey())
                    .append("=")
                    .append(val)
                    .append("\n");
            }
        }
        this.parameters = queryString.toString();

        User user = AuthFilter.getUser(request);
        if (user != null)
            this.userId = user.getId();

        this.action = e.getAction();
        this.ipAddress = DynActionForm.getHttpRequestRemoteAddr(request);
        this.duration = e.getDuration();
        this.error = Utils.maskNull(e.getError());
    }

    /**
     * @return user ID.
     */
    public int getUserId() {
        return userId;
    }

    public void setUserId(int value) {
        this.userId = value;
    }

    /**
     * @return calling IP address.
     */
    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    /**
     * @return semicolon separated action class and method.
     */
    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    /**
     * @return HTTP request parameters in form of query string.
     */
    public String getParameters() {
        return parameters == null ? "" : parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    /**
     * @return execution start time.
     */
    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    /**
     * @return action duration in milliseconds.
     */
    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    /**
     * @return execution error text or empty string for correct execution.
     */
    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
