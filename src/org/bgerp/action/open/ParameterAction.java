package org.bgerp.action.open;

import org.apache.struts.action.ActionForward;

import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path = "/open/parameter")
public class ParameterAction extends org.bgerp.action.ParameterAction {
    @Override
    public ActionForward parameterList(DynActionForm form, ConnectionSet conSet) throws Exception {
        super.parameterListInternal(form, conSet);
        return html(conSet, null, PATH_JSP + "/list.jsp");
    }
}
