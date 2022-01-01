package org.bgerp.action.admin;

import org.apache.struts.action.ActionForward;
import org.bgerp.scheduler.TasksConfig;

import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.RunClassRequestEvent;
import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.action.BaseAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path = "/admin/run")
public class RunAction extends BaseAction {
    private static final String PATH_JSP = PATH_JSP_ADMIN + "/run";

    @Override
    public ActionForward unspecified(DynActionForm form, ConnectionSet conSet)
            throws Exception {
        form.setRequestAttribute("runnableClasses", setup.getConfig(TasksConfig.class).getRunnableClasses());
        return html(conSet, form, PATH_JSP + "/run.jsp");
    }

    public ActionForward runClass(DynActionForm form, ConnectionSet conSet)
        throws Exception {
        String className = form.getParam("class");

        String ifaceType = form.getParam("iface", "event");
        // running interface EventListener
        if ("event".equals(ifaceType))
            EventProcessor.processEvent(new RunClassRequestEvent(form), className, conSet);
        // running interface Runnable
        else {
            Class<?> clazz = Class.forName(className);

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

        return json(conSet, form);
    }

    public ActionForward scheduler(DynActionForm form, ConnectionSet conSet) {
        // TODO: Finish later.
        /* var config = setup.getConfig(TasksConfig.class); */

        return html(conSet, form, PATH_JSP + "/scheduler.jsp");
    }
}
