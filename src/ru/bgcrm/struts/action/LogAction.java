package ru.bgcrm.struts.action;

import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForward;

import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.SessionLogAppender;
import ru.bgcrm.util.sql.ConnectionSet;

public class LogAction extends BaseAction {
    private static final String JSP = PATH_JSP_USER + "/log/log.jsp";

    @Override
    public ActionForward unspecified(DynActionForm form, ConnectionSet conSet)
            throws Exception {
        HttpSession session = form.getHttpRequest().getSession();
        
        form.setResponseData("state", SessionLogAppender.isSessionTracked(session));
        form.setResponseData("log", SessionLogAppender.getSessionLog(session));

        return html(conSet, form, JSP);
    }
    
    public ActionForward log(DynActionForm form, ConnectionSet conSet)
            throws Exception {
        boolean value = form.getParamBoolean("enable", false);
        
        HttpSession session = form.getHttpRequest().getSession();
        if (value)
            SessionLogAppender.trackSession(session, true);
        else
            SessionLogAppender.untrackSession(session);

        return unspecified(form, conSet);
    }
    
    public ActionForward download(DynActionForm form, ConnectionSet conSet)
            throws Exception {
        boolean value = form.getParamBoolean("enable", false);
        
        HttpSession session = form.getHttpRequest().getSession();
        if (value)
            SessionLogAppender.trackSession(session, true);
        else
            SessionLogAppender.untrackSession(session);

        return json(conSet, form);
    }
}