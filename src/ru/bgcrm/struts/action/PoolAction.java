package ru.bgcrm.struts.action;

import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForward;
import org.bgerp.servlet.user.LoginStat;

import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.GetPoolTasksEvent;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.sql.ConnectionSet;


@Action(path = "/user/pool")
public class PoolAction extends BaseAction {
    @Override
    public ActionForward unspecified(DynActionForm form, ConnectionSet con) throws Exception {
        EventProcessor.processEvent(new GetPoolTasksEvent(form), con);

        HttpSession session = form.getHttpRequest().getSession(false);
        if (session != null && !LoginStat.getLoginStat().isSessionValid(session))
            session.invalidate();

        return json(con, form);
    }
}
