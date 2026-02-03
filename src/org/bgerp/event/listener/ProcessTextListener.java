package org.bgerp.event.listener;

import java.sql.SQLException;

import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.event.EventProcessor;
import org.bgerp.dao.expression.Expression;
import org.bgerp.model.process.config.ProcessDescriptionConfig;
import org.bgerp.model.process.config.ProcessTitleConfig;
import org.bgerp.util.Log;

import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.event.ParamChangedEvent;
import ru.bgcrm.event.process.ProcessChangedEvent;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

/**
 * Performs process title and description updates
 *
 * @author Shamil Vakhitov
 */
public class ProcessTextListener {
    private static final Log log = Log.getLog();

    public ProcessTextListener() {
        EventProcessor.subscribe((e, conSet) -> processChanged(e, conSet), ProcessChangedEvent.class);
        EventProcessor.subscribe((e, conSet) -> paramChanged(e, conSet), ParamChangedEvent.class);
    }

    private void processChanged(ProcessChangedEvent e, ConnectionSet conSet) throws SQLException {
        var process = e.getProcess();

        ConfigMap configMap = process.getType().getProperties().getConfigMap();

        var titleConfig = configMap.getConfig(ProcessTitleConfig.class);
        if (titleConfig != null && titleConfig.isProcessUsed())
            updateProcessTitle(e.getForm(), conSet, process, titleConfig.getExpression());

        var descriptionConfig  = configMap.getConfig(ProcessDescriptionConfig.class);
        if (descriptionConfig != null && descriptionConfig.isProcessUsed())
            updateProcessDescription(e.getForm(), conSet, process, descriptionConfig.getExpression());
    }

    private void paramChanged(ParamChangedEvent e, ConnectionSet conSet) throws Exception {
        // the listener can preliminary scan all process types to store param IDs, used for title and description generation, and use that IDs in the condition
        if (!Process.OBJECT_TYPE.equals(e.getParameter().getObjectType()))
            return;

        var process = new ProcessDAO(conSet.getSlaveConnection()).getProcessOrThrow(e.getObjectId());
        ConfigMap configMap = process.getType().getProperties().getConfigMap();
        Integer paramId = e.getParameter().getId();

        var titleConfig = configMap.getConfig(ProcessTitleConfig.class);
        if (titleConfig != null && titleConfig.isParamUsed(paramId))
            updateProcessTitle(e.getForm(), conSet, process, titleConfig.getExpression());

        var descriptionConfig = configMap.getConfig(ProcessDescriptionConfig.class);
        if (descriptionConfig != null && descriptionConfig.isParamUsed(paramId))
            updateProcessDescription(e.getForm(), conSet, process, descriptionConfig.getExpression());
    }

    private void updateProcessTitle(DynActionForm form, ConnectionSet conSet, Process process, String expression) throws SQLException {
        var context = Expression.context(conSet, form, null, process);

        String title = Utils.maskNull(new Expression(context).executeGetString(expression));
        if (!title.equals(process.getTitle())) {
            new ProcessDAO(conSet.getConnection()).updateProcessTitle(process.getId(), title);
            form.getResponse().addEvent(new ru.bgcrm.event.client.ProcessChangedEvent(process.getId()));
            log.info("Update title '{}' for process {}", title, process.getId());
        }
    }

    private void updateProcessDescription(DynActionForm form, ConnectionSet conSet, Process process, String expression) throws SQLException {
        var context = Expression.context(conSet, form, null, process);

        String description = Utils.maskNull(new Expression(context).executeGetString(expression));
        if (!description.equals(process.getDescription())) {
            process.setDescription(description);
            new ProcessDAO(conSet.getConnection()).updateProcess(process);
            form.getResponse().addEvent(new ru.bgcrm.event.client.ProcessChangedEvent(process.getId()));
            log.info("Update description '{}' for process {}", description, process.getId());
        }
    }
}
