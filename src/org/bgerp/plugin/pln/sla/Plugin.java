package org.bgerp.plugin.pln.sla;

import java.sql.Connection;

import org.bgerp.app.event.process.queue.QueueColumnEvent;
import org.bgerp.plugin.pln.sla.config.Config;
import org.bgerp.plugin.pln.sla.config.ProcessTypeConfig;
import org.bgerp.plugin.pln.sla.model.process.queue.Column;
import org.bgerp.util.Log;

import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.process.ProcessChangedEvent;
import ru.bgcrm.util.Setup;

public class Plugin extends ru.bgcrm.plugin.Plugin {
    private static final Log log = Log.getLog();

    public static final String ID = "sla";
    public static final Plugin INSTANCE = new Plugin();

    public static final String PATH_JSP_USER = PATH_JSP_USER_PLUGIN + "/" + ID;

    private Plugin() {
        super(ID);
    }

    @Override
    public void init(Connection con) {
        EventProcessor.subscribe((e, conSet) -> {
            var config = Setup.getSetup().getConfig(Config.class);
            if (config == null)
                return;

            var typeConfig = e.getProcess().getType().getProperties().getConfigMap().getConfig(ProcessTypeConfig.class);
            if (typeConfig == null)
                return;

            if (e.isCreated())
                typeConfig.processCreated(conSet, config, e.getProcess().getId());
            else
                typeConfig.processUpdated(conSet, config, e.getProcess().getId());
        }, ProcessChangedEvent.class);

        EventProcessor.subscribe((e, conSet) -> {
            if (e.getColumn() != null)
                return;

            log.debug("Processing column value: {}", e.getColumnDefault().getValue());

            final String prefix = ID + ":";
            if (!e.getColumnDefault().getValue().startsWith(prefix))
                return;

            var config = Setup.getSetup().getConfig(Config.class);
            if (config == null)
                return;

            e.setColumn(new Column(e.getColumnDefault()));
        }, QueueColumnEvent.class);
    }
}
