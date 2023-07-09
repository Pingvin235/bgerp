package org.bgerp.action.admin;

import org.apache.struts.action.ActionForward;
import org.bgerp.app.bean.Bean;
import org.bgerp.app.scheduler.Scheduler;
import org.bgerp.app.scheduler.TasksConfig;

import ru.bgcrm.event.Event;
import ru.bgcrm.event.RunClassRequestEvent;
import ru.bgcrm.event.listener.EventListener;
import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.action.BaseAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path = "/admin/run")
public class RunAction extends BaseAction {
    private static final String PATH_JSP = PATH_JSP_ADMIN + "/run";

    @Override
    public ActionForward unspecified(DynActionForm form, ConnectionSet conSet) throws Exception {
        form.setRequestAttribute("taskClasses", setup.getConfig(TasksConfig.class).getTaskClasses());
        return html(conSet, form, PATH_JSP + "/run.jsp");
    }

    @SuppressWarnings("unchecked")
    public ActionForward runClass(DynActionForm form, ConnectionSet conSet) throws Exception {
        // 'class' is passed when value is choosen from drop-down, 'data' - entered directly
        String className = form.getParam("class");
        if (Utils.isBlankString(className))
            className = form.getParam("data");

        String ifaceType = form.getParam("iface", "event");
        // running interface EventListener
        if ("event".equals(ifaceType))
            ((EventListener<Event>) Bean.newInstance(className)).notify(new RunClassRequestEvent(form), conSet);
        // running interface Runnable
        else {
            Class<?> clazz = Bean.getClass(className);

            if (Runnable.class.isAssignableFrom(clazz)) {
                boolean sync = form.getParamBoolean("sync");
                if (sync)
                    ((Runnable) clazz.getDeclaredConstructor().newInstance()).run();
                else
                    new Thread((Runnable) clazz.getDeclaredConstructor().newInstance()).start();
            } else {
                throw new BGMessageException("The class does not implement java.lang.Runnable: {}", className);
            }
        }

        return json(conSet, form);
    }

    public ActionForward scheduler(DynActionForm form, ConnectionSet conSet) {
        form.setResponseData("scheduled", setup.getConfig(TasksConfig.class).getTaskConfigs());

        if (!Scheduler.getInstance().isAlive())
            form.setResponseData("error", l.l("Not Running"));

        return html(conSet, form, PATH_JSP + "/scheduler.jsp");
    }
}
