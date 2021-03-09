package org.bgerp.plugin.msg.sms;

import java.sql.Connection;

import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.listener.DefaultProcessChangeListener.DefaultProcessorChangeContextEvent;

public class Plugin extends ru.bgcrm.plugin.Plugin {
    public static final String ID = "sms";

    public Plugin() {
        super(ID);
    }

    @Override
    public void init(Connection con) throws Exception {
        super.init(con);

        EventProcessor.subscribe((e, conSet) -> {
            e.getContext().put(ID, new DefaultProcessorFunctions());
        }, DefaultProcessorChangeContextEvent.class);
    }
}
