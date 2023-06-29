package ru.bgcrm.struts.action.admin;

import org.apache.struts.action.ActionForward;
import org.bgerp.custom.java.CompilationResult;

import ru.bgcrm.dynamic.DynamicClassManager;
import ru.bgcrm.event.Event;
import ru.bgcrm.event.RunClassRequestEvent;
import ru.bgcrm.event.listener.EventListener;
import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.action.BaseAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path = "/admin/dynamic")
public class DynamicAction extends BaseAction {
    private static final String JSP = PATH_JSP_ADMIN + "/dynamic/dynamic.jsp";

    @Override
    public ActionForward unspecified(DynActionForm form, ConnectionSet conSet) {
        return html(conSet, form, JSP);
    }

    public ActionForward recompileAll(DynActionForm form, ConnectionSet conSet) throws Exception {
        CompilationResult result = DynamicClassManager.getInstance().recompileAll();

        form.getResponse().setData("result", result);

        return html(conSet, form, JSP);
    }

    // TODO: Is actively used, preserve when migrating to Custom.
    public ActionForward runDynamicClass(DynActionForm form, ConnectionSet conSet) throws Exception {
        String className = form.getParam("class");

        String ifaceType = form.getParam("iface", "event");
        if ("event".equals(ifaceType)) {
            @SuppressWarnings("unchecked")
            EventListener<Event> listener = (EventListener<Event>) Utils.newInstance(className);
            listener.notify(new RunClassRequestEvent(form), conSet);
        }
        // запуск интерфейса Runnable
        else {
            Class<?> clazz = null;
            try {
                clazz = DynamicClassManager.getClass(className);
            } catch (ClassNotFoundException e) {
                throw new BGMessageException("Класс не найден: {}", className);
            }

            if (Runnable.class.isAssignableFrom(clazz)) {
                boolean sync = form.getParamBoolean("sync");
                if (sync)
                    ((Runnable) clazz.getDeclaredConstructor().newInstance()).run();
                else
                    new Thread((Runnable) clazz.getDeclaredConstructor().newInstance()).start();
            } else {
                throw new BGMessageException("Класс не реализует java.land.Runnable: {}", className);
            }
        }

        return json(conSet, form);
    }
}