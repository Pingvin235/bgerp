package ru.bgcrm.plugin.task;

import java.sql.Connection;

import ru.bgcrm.dao.expression.Expression.ContextInitEvent;
import ru.bgcrm.event.EventProcessor;

public class Plugin extends ru.bgcrm.plugin.Plugin {
    public static final String ID = "task";

    public Plugin() {
        super(ID);
    }

    @Override
    public void init(Connection con) throws Exception {
        super.init(con);

        EventProcessor.subscribe((e, connectionSet) -> {
            e.getContext().put(ID, new DefaultProcessorFunctions());
        }, ContextInitEvent.class);
    }
}
