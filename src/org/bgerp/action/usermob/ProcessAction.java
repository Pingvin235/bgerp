package org.bgerp.action.usermob;

import java.sql.Connection;

import org.apache.struts.action.ActionForward;

import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.action.ProcessQueueAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path = "/usermob/process")
public class ProcessAction extends ProcessQueueAction {
    private static final String PATH_JSP = PATH_JSP_USERMOB + "/process";

    @Override
    public ActionForward unspecified(DynActionForm form, ConnectionSet conSet) throws Exception {
        super.process(form, conSet);
        return html(conSet, form, PATH_JSP + "/process/process.jsp");
    }

    @Override
    public ActionForward queue(DynActionForm form, ConnectionSet conSet) throws Exception {
        super.queue(form, conSet);
        return html(conSet, form, PATH_JSP + "/queue/queue.jsp");
    }

    @Override
    public ActionForward queueShow(DynActionForm form, ConnectionSet conSet) throws Exception {
        super.queueShow(form, conSet);
        return html(conSet, form, PATH_JSP + "/queue/show.jsp");
    }

    @Override
    public ActionForward processCreate(DynActionForm form, Connection con) throws Exception {
        return super.processCreate(form, con);
    }
}
