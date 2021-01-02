package ru.bgcrm.plugin.slack;

import java.sql.Connection;
import java.util.Set;

import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.listener.DefaultProcessChangeListener.DefaultProcessorChangeContextEvent;

public class Plugin extends ru.bgcrm.plugin.Plugin {
    public static final String ID = "slack";

    public static final String LINK_TYPE_CHANNEL = "slack-channel";

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

    @Override
    public Set<String> getObjectTypes() {
        return Set.of(LINK_TYPE_CHANNEL);
    }
}
