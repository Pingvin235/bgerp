package org.bgerp.action.open;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.action.BaseAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path = "/open/test")
public class TestAction extends BaseAction {
    @Override
    public ActionForward unspecified(ActionMapping mapping, DynActionForm form, ConnectionSet conSet)
            throws Exception {
        return html(conSet, null, PATH_JSP_OPEN + "/test/test.jsp");
    }

    // TODO: Some helper methods for testing parameters validatation and so on.
}
