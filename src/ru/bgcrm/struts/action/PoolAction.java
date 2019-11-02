package ru.bgcrm.struts.action;

import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.GetPoolTasksEvent;
import ru.bgcrm.servlet.LoginStat;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.sql.ConnectionSet;

public class PoolAction extends BaseAction {
    @Override
    protected ActionForward unspecified(ActionMapping mapping, DynActionForm form, ConnectionSet con) throws Exception {
        EventProcessor.processEvent(new GetPoolTasksEvent(form), con);

        HttpSession session = form.getHttpRequest().getSession(false);
        if (session != null && !LoginStat.getLoginStat().isSessionValid(session))
            session.invalidate();

        return processJsonForward(con, form);
    }
}
