package org.bgerp.event.listener;

import java.sql.SQLException;
import java.util.HashMap;

import org.bgerp.app.event.EventProcessor;
import org.bgerp.model.process.config.ProcessTitleConfig;
import org.bgerp.util.Log;

import ru.bgcrm.dao.expression.Expression;
import ru.bgcrm.dao.expression.ProcessExpressionObject;
import ru.bgcrm.dao.expression.ProcessParamExpressionObject;
import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.event.ParamChangedEvent;
import ru.bgcrm.event.process.ProcessChangedEvent;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

public class ProcessTitleListener {
    private static final Log log = Log.getLog();

    public ProcessTitleListener() {
        EventProcessor.subscribe((e, conSet) -> processChanged(e, conSet), ProcessChangedEvent.class);
        EventProcessor.subscribe((e, conSet) -> paramChanged(e, conSet), ParamChangedEvent.class);
    }

    private void processChanged(ProcessChangedEvent e, ConnectionSet conSet) {
        var process = e.getProcess();

        var config = process.getType().getProperties().getConfigMap().getConfig(ProcessTitleConfig.class);
        if (config == null || !config.isProcessUsed()) {
            log.debug("process {} isn't used for title generation", process.getId());
            return;
        }

        try {
            updateProcessTitle(e.getForm(), conSet, process, config);
        } catch (SQLException ex) {
            log.error(ex);
        }
    }

    private void paramChanged(ParamChangedEvent e, ConnectionSet conSet) {
        if (!Process.OBJECT_TYPE.equals(e.getParameter().getObjectType()))
            return;

        try {
            var process = new ProcessDAO(conSet.getSlaveConnection()).getProcessOrThrow(e.getObjectId());

            var config = process.getType().getProperties().getConfigMap().getConfig(ProcessTitleConfig.class);
            if (config == null || !config.getParamIds().contains(e.getParameter().getId())) {
                log.debug("'null' config or doesn't include param: {}", e.getParameter().getId());
                return;
            }

            updateProcessTitle(e.getForm(), conSet, process, config);
        } catch (Exception ex) {
            log.error(ex);
        }
    }

    private void updateProcessTitle(DynActionForm form, ConnectionSet conSet, Process process, ProcessTitleConfig config) throws SQLException {
        var context = new HashMap<String, Object>();
        new ProcessExpressionObject(process).toContext(context);
        new ProcessParamExpressionObject(conSet.getConnection(), process.getId()).toContext(context);

        String title = Utils.maskNull(new Expression(context).executeGetString(config.getExpression()));
        if (!title.equals(process.getTitle())) {
            new ProcessDAO(conSet.getConnection()).updateProcessTitle(process.getId(), title);
            form.getResponse().addEvent(new ru.bgcrm.event.client.ProcessChangedEvent(process.getId()));
            log.info("Update title '{}' for process {}", title, process.getId());
        }
    }
}
