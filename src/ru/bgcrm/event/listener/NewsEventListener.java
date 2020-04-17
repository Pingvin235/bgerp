package ru.bgcrm.event.listener;

import ru.bgcrm.cache.UserNewsCache;
import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.GetPoolTasksEvent;
import ru.bgcrm.event.client.NewsInfoEvent;
import ru.bgcrm.util.sql.ConnectionSet;

public class NewsEventListener {
    public NewsEventListener() {
        EventProcessor.subscribe(new EventListener<GetPoolTasksEvent>() {
            @Override
            public void notify(GetPoolTasksEvent e, ConnectionSet connectionSet) throws Exception {
                processEvent(connectionSet, e);
            }

        }, GetPoolTasksEvent.class);
    }

    private void processEvent(ConnectionSet connectionSet, GetPoolTasksEvent e) throws Exception {
        NewsInfoEvent event = UserNewsCache.getUserEvent(connectionSet.getConnection(), e.getUser().getId());
        e.getForm().getResponse().addEvent(event);
    }
}
