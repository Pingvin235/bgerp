package org.bgerp.event.listener;

import java.util.HashMap;

import org.bgerp.app.event.EventProcessor;
import org.bgerp.model.process.config.ProcessTitleConfig;
import org.bgerp.util.Log;

import ru.bgcrm.dao.expression.Expression;
import ru.bgcrm.dao.expression.ProcessExpressionObject;
import ru.bgcrm.dao.expression.ProcessParamExpressionObject;
import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.event.ParamChangedEvent;
import ru.bgcrm.event.client.ProcessChangedEvent;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

public class ProcessTitleListener {
    private static final Log log = Log.getLog();

    public ProcessTitleListener() {
        EventProcessor.subscribe((e, conSet) -> paramChanged(e, conSet), ParamChangedEvent.class);
    }

    private void paramChanged(ParamChangedEvent e, ConnectionSet conSet) {
        if (!Process.OBJECT_TYPE.equals(e.getParameter().getObjectType()))
            return;

        try {
            ProcessDAO dao = new ProcessDAO(conSet.getConnection());

            var process = dao.getProcessOrThrow(e.getObjectId());

            var config = process.getType().getProperties().getConfigMap().getConfig(ProcessTitleConfig.class);
            if (config == null || !config.getParamIds().contains(e.getParameter().getId())) {
                log.debug("'null' config or doesn't include param: {}", e.getParameter().getId());
                return;
            }

            var context = new HashMap<String, Object>();
            new ProcessExpressionObject(process).toContext(context);
            new ProcessParamExpressionObject(conSet.getConnection(), e.getObjectId()).toContext(context);

            String title = Utils.maskNull(new Expression(context).executeGetString(config.getExpression()));
            if (!title.equals(process.getTitle())) {
                dao.updateProcessTitle(e.getObjectId(), title);
                e.getForm().getResponse().addEvent(new ProcessChangedEvent(e.getObjectId()));
                log.info("Update title '{}' for process {}", title, e.getObjectId());
            }
        } catch (Exception ex) {
            log.error(ex);
        }
    }
}
