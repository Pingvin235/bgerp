package org.bgerp.plugin.pln.agree;

import java.sql.Connection;

import org.bgerp.app.event.EventProcessor;
import org.bgerp.util.Log;

import ru.bgcrm.event.process.ProcessChangedEvent;
import ru.bgcrm.model.process.Process;

public class Plugin extends ru.bgcrm.plugin.Plugin {
    private static final Log log = Log.getLog();

    public static final String ID = "agree";
    public static final Plugin INSTANCE = new Plugin();

    private Plugin() {
        super(ID);
    }

    @Override
    public String getTitle() {
        return "Agree";
    }

    @Override
    public void init(Connection con) throws Exception {
        super.init(con);

        EventProcessor.subscribe((e, conSet) -> {
            if (!e.isStatus())
                return;

            Process process = e.getProcess();

            var typeConfig = process.getType().getProperties().getConfigMap().getConfig(ProcessTypeConfig.class);
            if (typeConfig == null)
                return;

            log.debug("Agree status changing for process {}", process.getId());

            typeConfig.statusChanged(e.getForm(), conSet, process);
        }, ProcessChangedEvent.class);
    }
}
