package ru.bgcrm.struts.action.admin;

import org.apache.struts.action.ActionForward;
import org.bgerp.action.BaseAction;
import org.bgerp.app.bean.Bean;
import org.bgerp.app.exception.BGMessageException;

import ru.bgcrm.event.Event;
import ru.bgcrm.event.RunClassRequestEvent;
import ru.bgcrm.event.listener.EventListener;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path = "/admin/dynamic")
public class DynamicAction extends BaseAction {
    public ActionForward runDynamicClass(DynActionForm form, ConnectionSet conSet) throws Exception {
        log.warnd("Used deprecated 'runDynamicClass' action call, use 'runClass' instead.");

        String className = form.getParam("class");

        String ifaceType = form.getParam("iface", "event");
        if ("event".equals(ifaceType)) {
            @SuppressWarnings("unchecked")
            EventListener<Event> listener = (EventListener<Event>) Bean.newInstance(className);
            listener.notify(new RunClassRequestEvent(form), conSet);
        }
        // запуск интерфейса Runnable
        else {
            Class<?> clazz = null;
            try {
                clazz = Bean.getClass(className);
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