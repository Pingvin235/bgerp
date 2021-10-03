package org.bgerp.plugin.telegram;

import java.sql.Connection;

import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.dao.expression.Expression.ContextInitEvent;

public class Plugin extends ru.bgcrm.plugin.Plugin {
    public static final String ID = "telegram";

    public Plugin() {
        super(ID);
    }

    @Override
    public void init(Connection con) throws Exception {
        super.init(con);

        Bot.getInstance();

        EventProcessor.subscribe((e, conSet) -> {
            e.getContext().put(ID, new ExpressionBean());
        }, ContextInitEvent.class);
    }
}
