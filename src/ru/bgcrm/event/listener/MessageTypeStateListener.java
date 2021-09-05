package ru.bgcrm.event.listener;

import ru.bgcrm.cache.UserNewsCache;
import ru.bgcrm.dao.message.config.MessageTypeConfig;
import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.GetPoolTasksEvent;
import ru.bgcrm.event.client.NewsInfoEvent;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.sql.ConnectionSet;

public class MessageTypeStateListener {
    public MessageTypeStateListener() {
        EventProcessor.subscribe((e, conSet) -> processEvent(conSet, e), GetPoolTasksEvent.class);
    }

    private void processEvent(ConnectionSet connectionSet, GetPoolTasksEvent e) throws Exception {
        var config = Setup.getSetup().getConfig(MessageTypeConfig.class);
        //for 

        /* NewsInfoEvent event = UserNewsCache.getUserEvent(connectionSet.getConnection(), e.getUser().getId());
        e.getForm().getResponse().addEvent(event); */
    }
}
