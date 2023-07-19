package ru.bgcrm.event.listener;

import org.bgerp.action.admin.AppAction;
import org.bgerp.app.dist.inst.VersionCheck;
import org.bgerp.event.client.NewsInfoEvent;

import ru.bgcrm.cache.UserNewsCache;
import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.GetPoolTasksEvent;
import ru.bgcrm.util.sql.ConnectionSet;

public class NewsEventListener {
    public NewsEventListener() {
        EventProcessor.subscribe((e, conSet) -> processEvent(conSet, e), GetPoolTasksEvent.class);
    }

    private void processEvent(ConnectionSet conSet, GetPoolTasksEvent e) throws Exception {
        NewsInfoEvent event = UserNewsCache.getUserEvent(conSet.getConnection(), e.getUser().getId());

        if (VersionCheck.INSTANCE.isUpdateNeeded()) {
            event.version(e.getUser().checkPerm(AppAction.class.getName() + ":status"));
            event.message(e.getForm().l, "App update is needed");
        }

        e.getForm().getResponse().addEvent(event);
    }
}
