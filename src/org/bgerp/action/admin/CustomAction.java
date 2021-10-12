package org.bgerp.action.admin;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.bgerp.custom.Custom;

import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.action.BaseAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path = "/admin/custom")
public class CustomAction extends BaseAction {
    public static final String PATH_JSP = PATH_JSP_ADMIN + "/custom";

    private static final String JSP_CUSTOM = PATH_JSP + "/custom.jsp";

    @Override
    protected ActionForward unspecified(ActionMapping mapping, DynActionForm form, ConnectionSet conSet)
            throws Exception {
        return html(conSet, form, JSP_CUSTOM);
    }

    public ActionForward compile(ActionMapping mapping, DynActionForm form, ConnectionSet conSet)
            throws Exception {
        var result = Custom.getInstance().compileJava();
        form.setResponseData("result", result);

        return html(conSet, form, JSP_CUSTOM);
    }
}
