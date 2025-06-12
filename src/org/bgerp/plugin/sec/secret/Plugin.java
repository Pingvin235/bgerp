package org.bgerp.plugin.sec.secret;

import java.sql.Connection;
import java.util.Set;

import org.bgerp.app.event.EventProcessor;
import org.bgerp.dao.expression.Expression.ContextInitEvent;
import org.bgerp.plugin.sec.secret.dao.Tables;

import ru.bgcrm.plugin.Table;
import ru.bgcrm.plugin.Table.Type;

public class Plugin extends ru.bgcrm.plugin.Plugin {
    public static final String ID = "secret";
    public static final Plugin INSTANCE = new Plugin();

    private Plugin() {
        super(ID);
    }

    @Override
    public Set<Table> getTables() {
        return Set.of(new Table(Tables.TABLE_SECRET_OPEN, Type.TRASH));
    }

    @Override
    public void init(Connection con) throws Exception {
        super.init(con);

        EventProcessor.subscribe((e, conSet) -> {
            e.getContext().put(ID, new ExpressionObject());
        }, ContextInitEvent.class);
    }
}
