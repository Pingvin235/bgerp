package ru.bgcrm.event.listener;

import org.bgerp.action.admin.AppAction;
import org.bgerp.app.dist.inst.VersionCheck;
import org.bgerp.app.event.EventProcessor;
import org.bgerp.cache.UserNewsCache;
import org.bgerp.event.client.NewsInfoEvent;

import ru.bgcrm.event.GetPoolTasksEvent;
import ru.bgcrm.servlet.ActionServlet;
import ru.bgcrm.util.sql.ConnectionSet;

public class NewsEventListener {
    private static final String ACTION_STATUS = ActionServlet.pathId(AppAction.class, "status");

    public NewsEventListener() {
        EventProcessor.subscribe((e, conSet) -> processEvent(conSet, e), GetPoolTasksEvent.class);
    }

    private void processEvent(ConnectionSet conSet, GetPoolTasksEvent e) throws Exception {
        NewsInfoEvent event = UserNewsCache.getUserEvent(conSet, e.getUser().getId());

        if (VersionCheck.INSTANCE.isUpdateNeeded()) {
            event.version(e.getUser().checkPerm(ACTION_STATUS));
            event.message(e.getForm().l, "App update is needed");
        }

        e.getForm().getResponse().addEvent(event);
    }
}
