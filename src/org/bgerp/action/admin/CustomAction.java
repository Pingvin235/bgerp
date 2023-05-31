package org.bgerp.action.admin;

import org.apache.struts.action.ActionForward;
import org.bgerp.custom.Custom;
import org.bgerp.app.servlet.file.Files;
import org.bgerp.app.servlet.file.Options;
import org.bgerp.app.servlet.file.Order;

import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.action.BaseAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path = "/admin/custom")
public class CustomAction extends BaseAction {
    public static final String PATH_JSP = PATH_JSP_ADMIN + "/custom";

    private static final String JSP_CUSTOM = PATH_JSP + "/custom.jsp";

    /** Accessed from JSP. */
    public static final Files CUSTOM_SRC = new Files(CustomAction.class, "custom", "custom", "*",
            new Options().withDownloadEnabled().withOrder(Order.NORMAL_FS));
    public static final Files CUSTOM_JAR = new Files(CustomAction.class, "customJar", "lib/app", "custom.jar",
            new Options().withDeletionEnabled());

    @Override
    public ActionForward unspecified(DynActionForm form, ConnectionSet conSet) {
        return html(conSet, form, JSP_CUSTOM);
    }

    public ActionForward downloadCustom(DynActionForm form, ConnectionSet conSet) throws Exception {
        return CUSTOM_SRC.download(form);
    }

    public ActionForward deleteCustomJar(DynActionForm form, ConnectionSet conSet) throws Exception {
        CUSTOM_JAR.delete(form);
        return json(conSet, form);
    }

    public ActionForward compile(DynActionForm form, ConnectionSet conSet) throws Exception {
        var result = Custom.INSTANCE.compileJava();
        form.setResponseData("result", result);

        if (result.isResult()) {
            // any key can be used here, the config map has been flushed on a put
            setup.put("flush.config.map", "");
        }

        return html(conSet, form, JSP_CUSTOM);
    }
}
