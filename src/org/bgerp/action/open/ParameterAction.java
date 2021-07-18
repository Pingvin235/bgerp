package org.bgerp.action.open;

import java.sql.Connection;

import org.apache.struts.action.ActionForward;

import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;

@Action(path = "/open/parameter")
public class ParameterAction extends ru.bgcrm.struts.action.ParameterAction {
    @Override
    public ActionForward parameterList(DynActionForm form, Connection con) throws Exception {
        super.parameterListInternal(form, con);
        return html(con, null, JSP_PATH + "/list.jsp");
    }
}
