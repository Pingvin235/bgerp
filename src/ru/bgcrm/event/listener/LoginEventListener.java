package ru.bgcrm.event.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.GetPoolTasksEvent;
import ru.bgcrm.event.client.ClientEvent;
import ru.bgcrm.model.BGException;
import ru.bgcrm.util.sql.ConnectionSet;

public class LoginEventListener {
    // событие которые будут переданы клиенту на первый pool запрос после авторизации
    private static Map<Integer, List<ClientEvent>> onLoginEvents = new ConcurrentHashMap<Integer, List<ClientEvent>>();

    public LoginEventListener() {
        EventProcessor.subscribe(new EventListener<GetPoolTasksEvent>() {
            @Override
            public void notify(GetPoolTasksEvent e, ConnectionSet connectionSet) throws BGException {
                processEvent(connectionSet, e);
            }

        }, GetPoolTasksEvent.class);
    }

    public static void addOnLoginEvent(int userId, ClientEvent event) {
        List<ClientEvent> eventList = onLoginEvents.get(userId);
        if (eventList == null) {
            onLoginEvents.put(userId, eventList = new ArrayList<ClientEvent>());
        }
        eventList.add(event);
    }

    private void processEvent(ConnectionSet connectionSet, GetPoolTasksEvent e) throws BGException {
        List<ClientEvent> eventList = onLoginEvents.remove(e.getUser().getId());
        if (eventList != null) {
            for (ClientEvent event : eventList) {
                e.getForm().getResponse().addEvent(event);
            }
        }
    }
}
