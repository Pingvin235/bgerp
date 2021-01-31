package org.bgerp.action.admin;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.bgerp.plugin.kernel.Plugin;

import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.RunClassRequestEvent;
import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.action.BaseAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path = "/admin/run")
public class RunAction extends BaseAction {
    public static final String JSP_PATH = Plugin.PATH_JSP_ADMIN + "/run";

    private static final String JSP_CUSTOM = JSP_PATH + "/run.jsp";

    @Override
    protected ActionForward unspecified(ActionMapping mapping, DynActionForm form, ConnectionSet conSet)
            throws Exception {
        return data(conSet, form, JSP_CUSTOM);
    }
    
    public ActionForward runClass(ActionMapping mapping, DynActionForm form, ConnectionSet conSet)
        throws Exception {
        String className = form.getParam("class");

        String ifaceType = form.getParam("iface", "event");
        // running interface EventListener
        if ("event".equals(ifaceType))
            EventProcessor.processEvent(new RunClassRequestEvent(form), className, conSet);
        // running interface Runnable
        else {
            Class<?> clazz = null;
            try {
                clazz = Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new BGMessageException("Класс не найден: %s", className);
            }

            if (Runnable.class.isAssignableFrom(clazz)) {
                boolean sync = form.getParamBoolean("sync");
                if (sync)
                    ((Runnable) clazz.getDeclaredConstructor().newInstance()).run();
                else
                    new Thread((Runnable) clazz.getDeclaredConstructor().newInstance()).start();
            } else {
                throw new BGMessageException("Класс не реализует java.lang.Runnable: %s", className);
            }
        }

        return status(conSet, form);
    }
}
