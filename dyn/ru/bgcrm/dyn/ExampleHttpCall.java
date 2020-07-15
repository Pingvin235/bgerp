package ru.bgcrm.dyn;

import org.apache.log4j.Logger;

import ru.bgcrm.event.Event;
import ru.bgcrm.event.RunClassRequestEvent;
import ru.bgcrm.event.listener.DynamicEventListener;
import ru.bgcrm.model.BGException;
import ru.bgcrm.util.sql.ConnectionSet;

/**
 * Демонстрационный динамический класс, может быть вызван HTTP запросом.
 * http://[host]:[port]/admin/dynamic.do?action=runDynamicClass&iface=event&class=ru.bgcrm.dyn.ExampleHttpCall&j_username=[user]&j_password=[pswd]&param1=value1
 */
public class ExampleHttpCall extends DynamicEventListener {
    private static final Logger log = Logger.getLogger(ExampleHttpCall.class);

    @Override
    public void notify(Event e, ConnectionSet connectionSet) throws BGException {
        RunClassRequestEvent event = (RunClassRequestEvent) e;

        String param1 = event.getForm().getParam("param1");
        log.info("Получен запрос с param1=" + param1);

        // в тестовых целях в ответе высылается объект с текущим пользователем
        event.getForm().setResponseData("user", event.getUser());
    }
}
