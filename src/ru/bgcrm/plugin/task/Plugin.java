package ru.bgcrm.plugin.task;

import java.sql.Connection;

import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.listener.DefaultProcessChangeListener.DefaultProcessorChangeContextEvent;
import ru.bgcrm.util.sql.ConnectionSet;

public class Plugin extends ru.bgcrm.plugin.Plugin {
    public static final String ID = "task";

    public Plugin() {
        super(ID);
    }

    @Override
    public void init(Connection con) throws Exception {
        super.init(con);

        EventProcessor.subscribe((DefaultProcessorChangeContextEvent e, ConnectionSet connectionSet) -> {
            e.getContext().put(ID, new DefaultProcessorFunctions());
        }, DefaultProcessorChangeContextEvent.class);
    }
}
