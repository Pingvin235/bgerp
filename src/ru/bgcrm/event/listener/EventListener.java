package ru.bgcrm.event.listener;

import ru.bgcrm.event.Event;
import ru.bgcrm.util.sql.ConnectionSet;

public interface EventListener<T extends Event> {
    public void notify(T e, ConnectionSet conSet) throws Exception;
}
