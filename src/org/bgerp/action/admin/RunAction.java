package org.bgerp.action.admin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.struts.action.ActionForward;
import org.bgerp.action.base.BaseAction;
import org.bgerp.app.bean.Bean;
import org.bgerp.app.event.iface.Event;
import org.bgerp.app.event.iface.EventListener;
import org.bgerp.app.exception.BGMessageException;
import org.bgerp.app.exec.Runnable;
import org.bgerp.app.exec.scheduler.Scheduler;
import org.bgerp.app.exec.scheduler.TasksConfig;
import org.bgerp.model.base.IdStringTitle;

import ru.bgcrm.event.RunClassRequestEvent;
import ru.bgcrm.plugin.PluginManager;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path = "/admin/run")
public class RunAction extends BaseAction {
    private static final String PATH_JSP = PATH_JSP_ADMIN + "/run";

    /** List of runnable classes, ID and titles are class names. Those classes extend {@link Runnable} and placed in application {@link PluginManager#ERP_PACKAGES}. */
    private static List<IdStringTitle> runnableClasses;

    @Override
    public ActionForward unspecified(DynActionForm form, ConnectionSet conSet) throws Exception {
        form.setRequestAttribute("runnableClasses", runnableClasses());
        return html(conSet, form, PATH_JSP + "/run.jsp");
    }

    private List<IdStringTitle> runnableClasses() {
        synchronized (PATH_JSP) {
            if (RunAction.runnableClasses == null) {
                var runnableClasses = new ArrayList<IdStringTitle>(100);

                var r = Bean.classes();
                for (Class<?> runnableClass : r.getSubTypesOf(Runnable.class)) {
                    var name = runnableClass.getName();
                    runnableClasses.add(new IdStringTitle(name, name));
                    log.debug("Found runnable class: {}", name);
                }

                Collections.sort(runnableClasses, (c1, c2) -> c1.getId().compareTo(c2.getId()));

                RunAction.runnableClasses = Collections.unmodifiableList(runnableClasses);
            }
        }
        return RunAction.runnableClasses;
    }

    @SuppressWarnings("unchecked")
    public ActionForward runClass(DynActionForm form, ConnectionSet conSet) throws Exception {
        // 'class' is passed when value is chosen from drop-down, 'data' - entered directly
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

            if (java.lang.Runnable.class.isAssignableFrom(clazz)) {
                boolean sync = form.getParamBoolean("sync");
                if (sync)
                    ((java.lang.Runnable) clazz.getDeclaredConstructor().newInstance()).run();
                else
                    new Thread((java.lang.Runnable) clazz.getDeclaredConstructor().newInstance()).start();
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

        form.setResponseData("runningCount", Scheduler.getInstance().getRunningTaskCount());

        return html(conSet, form, PATH_JSP + "/scheduler.jsp");
    }

    public ActionForward schedulerRun(DynActionForm form, ConnectionSet conSet) throws Exception {
        var taskConfig = setup.getConfig(TasksConfig.class).getTaskConfigOrThrow(form.getParam("id"));

        Scheduler.getInstance().runTask(taskConfig, form.getParamBoolean("wait"));

        return json(conSet, form);
    }
}
