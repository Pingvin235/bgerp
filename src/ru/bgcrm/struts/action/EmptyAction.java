package ru.bgcrm.struts.action;

import java.sql.Connection;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.bgcrm.struts.form.DynActionForm;

/**
 * Специальный акшен, ничего не делает - а передаёт управление на JSP ку, 
 * указанную в параметрах запроса.
 */
public class EmptyAction extends BaseAction {
    public EmptyAction() {
        super();
    }

    @Override
    protected ActionForward unspecified(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        HttpServletRequest request = form.getHttpRequest();
        
        if (log.isDebugEnabled())
            log.debug("r:" + request + "; f:" + form + "; q: " + request.getQueryString());

        return json(con, form);
    }
}
