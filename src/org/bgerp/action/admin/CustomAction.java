package org.bgerp.action.admin;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.bgerp.custom.Custom;
import org.bgerp.plugin.kernel.Plugin;

import ru.bgcrm.struts.action.BaseAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.sql.ConnectionSet;

public class CustomAction extends BaseAction {
    public static final String JSP_PATH = Plugin.PATH_JSP_ADMIN + "/custom";

    private static final String JSP_CUSTOM = JSP_PATH + "/custom.jsp";

    @Override
    protected ActionForward unspecified(ActionMapping mapping, DynActionForm form, ConnectionSet conSet)
            throws Exception {
        return data(conSet, form, JSP_CUSTOM);
    }
    
    public ActionForward compile(ActionMapping mapping, DynActionForm form, ConnectionSet conSet)
            throws Exception {
        var result = Custom.getInstance().compileJava();
        form.setResponseData("result", result);

        return data(conSet, form, JSP_CUSTOM);
    }
}
