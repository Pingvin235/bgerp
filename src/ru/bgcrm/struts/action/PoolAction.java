package ru.bgcrm.struts.action;

import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForward;
import org.bgerp.action.base.BaseAction;
import org.bgerp.app.event.EventProcessor;
import org.bgerp.app.servlet.user.LoginStat;

import ru.bgcrm.event.GetPoolTasksEvent;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.sql.ConnectionSet;


@Action(path = "/user/pool")
public class PoolAction extends BaseAction {
    @Override
    public ActionForward unspecified(DynActionForm form, ConnectionSet conSet) throws Exception {
        EventProcessor.processEvent(new GetPoolTasksEvent(form), conSet);

        HttpSession session = form.getHttpRequest().getSession(false);
        if (session != null && !LoginStat.instance().isSessionValid(session))
            session.invalidate();

        return json(conSet, form);
    }
}
