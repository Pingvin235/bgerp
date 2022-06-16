package org.bgerp.action.open;

import org.apache.struts.action.ActionForward;

import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.action.BaseAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path = "/open/test")
public class TestAction extends BaseAction {
    @Override
    public ActionForward unspecified(DynActionForm form, ConnectionSet conSet) {
        return html(conSet, null, PATH_JSP_OPEN + "/test/test.jsp");
    }

    // TODO: Some helper methods for testing parameters validatation and so on.
}
