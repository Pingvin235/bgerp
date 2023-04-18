package org.bgerp.plugin.msg.sms;

import java.sql.Connection;

import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.dao.expression.Expression.ContextInitEvent;

public class Plugin extends ru.bgcrm.plugin.Plugin {
    public static final String ID = "sms";
    public static final Plugin INSTANCE = new Plugin();

    private Plugin() {
        super(ID);
    }

    @Override
    public String getTitle() {
        return "SMS";
    }

    @Override
    public void init(Connection con) throws Exception {
        super.init(con);

        EventProcessor.subscribe((e, conSet) -> {
            e.getContext().put(ID, new ExpressionObject());
        }, ContextInitEvent.class);
    }
}
