package ru.bgcrm.plugin.task;

import org.w3c.dom.Document;

import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.listener.DefaultProcessChangeListener.DefaultProcessorChangeContextEvent;
import ru.bgcrm.util.sql.ConnectionSet;

public class Plugin extends ru.bgcrm.plugin.Plugin {
    public static final String ID = "task";

    public Plugin(Document doc) {
        super(doc, ID);
        EventProcessor.subscribe((DefaultProcessorChangeContextEvent e, ConnectionSet connectionSet) -> {
            e.getContext().put(ID, new DefaultProcessorFunctions());
        }, DefaultProcessorChangeContextEvent.class);
    }
}
