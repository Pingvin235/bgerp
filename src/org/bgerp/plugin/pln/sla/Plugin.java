package org.bgerp.plugin.pln.sla;

import java.sql.Connection;
import java.util.HashSet;
import java.util.Set;

import org.bgerp.app.cfg.Setup;
import org.bgerp.event.process.queue.QueueColumnEvent;
import org.bgerp.plugin.pln.sla.config.Config;
import org.bgerp.plugin.pln.sla.config.ProcessTypeConfig;
import org.bgerp.plugin.pln.sla.model.process.queue.Column;
import org.bgerp.util.Log;

import ru.bgcrm.cache.ProcessTypeCache;
import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.ParamChangedEvent;
import ru.bgcrm.event.process.ProcessChangedEvent;
import ru.bgcrm.model.process.ProcessType;
import ru.bgcrm.model.process.TypeProperties;

public class Plugin extends ru.bgcrm.plugin.Plugin {
    private static final Log log = Log.getLog();

    public static final String ID = "sla";
    public static final Plugin INSTANCE = new Plugin();

    public static final String PATH_JSP_USER = PATH_JSP_USER_PLUGIN + "/" + ID;

    private Plugin() {
        super(ID);
    }

    @Override
    public String getTitle() {
        return "SLA";
    }

    @Override
    public void init(Connection con) throws Exception {
        super.init(con);

        var config = Setup.getSetup().getConfig(Config.class);
        if (config == null)
            return;

        log.debug("SLA config is defined");

        // process changes
        EventProcessor.subscribe((e, conSet) -> {
            var typeConfig = e.getProcess().getType().getProperties().getConfigMap().getConfig(ProcessTypeConfig.class);
            if (typeConfig == null)
                return;

            if (e.isCreated())
                typeConfig.processCreated(conSet, config, e.getProcess().getId());
            else
                typeConfig.processUpdated(conSet, config, e.getProcess().getId());
        }, ProcessChangedEvent.class);


        // parameters changes
        Set<Integer> processParamIds = new HashSet<>();

        for (ProcessType type : ProcessTypeCache.getProcessTypeMap().values()) {
            TypeProperties properties = type.getProperties();

            if (properties.getConfigMap().getConfig(ProcessTypeConfig.class) == null)
                continue;

            processParamIds.addAll(properties.getParameterIds());
        }

        EventProcessor.subscribe((e, conSet) -> {
            if (!processParamIds.contains(e.getParameter().getId()))
                return;

            var p = new ProcessDAO(conSet.getSlaveConnection()).getProcessOrThrow(e.getObjectId());

            var typeConfig = p.getType().getProperties().getConfigMap().getConfig(ProcessTypeConfig.class);
            if (typeConfig == null)
                log.debug("Param {} was changed for not SLA process {}", e.getParameter().getId(), e.getObjectId());
            else
                typeConfig.processUpdated(conSet, config, p.getId());

        }, ParamChangedEvent.class);

        // process queue column
        EventProcessor.subscribe((e, conSet) -> {
            if (e.getColumn() != null)
                return;

            log.debug("Processing column value: {}", e.getColumnDefault().getValue());

            final String prefix = ID + ":";
            if (!e.getColumnDefault().getValue().startsWith(prefix))
                return;

            e.setColumn(new Column(e.getColumnDefault()));
        }, QueueColumnEvent.class);
    }
}
