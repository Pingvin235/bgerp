package org.bgerp.action.usermob;

import java.sql.Connection;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.bgcrm.struts.action.ProcessQueueAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.sql.ConnectionSet;

public class ProcessAction extends ProcessQueueAction {

    @Override
    protected ActionForward unspecified(ActionMapping mapping, DynActionForm actionForm, Connection con) throws Exception {
        return super.unspecified(mapping, actionForm, con);
    }

    @Override
    public ActionForward queue(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws Exception {
        return super.queue(mapping, form, conSet);
    }

    @Override
    public ActionForward queueShow(ActionMapping mapping, DynActionForm form, ConnectionSet connectionSet) throws Exception {
        return super.queueShow(mapping, form, connectionSet);
    }

}
