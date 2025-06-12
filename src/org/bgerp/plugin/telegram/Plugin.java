package org.bgerp.plugin.telegram;

import java.sql.Connection;

import org.bgerp.app.event.EventProcessor;
import org.bgerp.dao.expression.Expression.ContextInitEvent;

public class Plugin extends ru.bgcrm.plugin.Plugin {
    public static final String ID = "telegram";
    public static final Plugin INSTANCE = new Plugin();

    private Plugin() {
        super(ID);
    }

    @Override
    public void init(Connection con) throws Exception {
        super.init(con);

        Bot.getInstance();

        EventProcessor.subscribe((e, conSet) -> {
            new ExpressionObject().toContext(e.getContext());
        }, ContextInitEvent.class);
    }
}
