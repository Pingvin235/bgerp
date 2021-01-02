package ru.bgerp.plugin.telegram;

import java.sql.Connection;

import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.listener.DefaultProcessChangeListener.DefaultProcessorChangeContextEvent;
import ru.bgerp.plugin.telegram.bot.BgerpBot;

public class Plugin extends ru.bgcrm.plugin.Plugin {
    public static final String ID = "telegram";

    public Plugin() {
        super(ID);
    }

    @Override
    public void init(Connection con) throws Exception {
        super.init(con);

        BgerpBot.getInstance();

        EventProcessor.subscribe((e, conSet) -> {
            e.getContext().put(ID, new DefaultProcessorFunctions());
        }, DefaultProcessorChangeContextEvent.class);
    }
}
