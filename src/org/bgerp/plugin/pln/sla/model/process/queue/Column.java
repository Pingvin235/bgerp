package org.bgerp.plugin.pln.sla.model.process.queue;

import static ru.bgcrm.dao.Tables.TABLE_PARAM_DATETIME;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.bgerp.plugin.pln.sla.config.Config;
import org.bgerp.plugin.pln.sla.config.ProcessTypeConfig;
import org.bgerp.util.Log;

import ru.bgcrm.model.process.Process;
import ru.bgcrm.util.Setup;

public class Column extends org.bgerp.model.process.queue.Column {
    private static final Log log = Log.getLog();

    public Column(org.bgerp.model.process.queue.Column columnDefault) {
        super(columnDefault);
    }

    @Override
    public void addQuery(StringBuilder selectPart, StringBuilder joinPart) throws Exception {
        var config = Setup.getSetup().getConfig(Config.class);
        if (config == null)
            return;

        String target = getProcess();

        int paramId = 0;
        String tableAlias = "";

        String suffix = StringUtils.substringAfter(getValue(), ":");
        if ("closeBefore".equals(suffix)) {
            paramId = config.getParamCloseBeforeId();
            tableAlias = "sla_param_close_before";
        } else if ("updateBefore".equals(suffix)) {
            paramId = config.getParamUpdateBeforeId();
            tableAlias = "sla_param_update_before";
        } else
            log.error("Incorrect suffix: {}", suffix);

        if (paramId > 0) {
            selectPart.append(tableAlias + ".value").append(" , ");
            joinPart.append(" LEFT JOIN " + TABLE_PARAM_DATETIME + " AS " + tableAlias + " ON " + target + ".id=" + tableAlias + ".id AND "
                    + tableAlias + ".param_id=" + paramId);
        }
    }

    private long leftMinutes(Date value) {
        return Duration.between(Instant.now(), value.toInstant()).toMinutes();
    }

    @Override
    public String cellHtml(Process process, Object col) {
        var type = process.getType();
        var configMap = type.getProperties().getConfigMap();

        var config = configMap.getConfig(ProcessTypeConfig.class);
        if (config == null) {
            log.warn("No SLA config defined for process type ID: {}, title: {}, config: {}", type.getId(), type.getTitle(), configMap);
            return null;
        }

        Date time = (Date) col;
        if (time == null)
            return null;

        long leftMinutes = leftMinutes(time);
        String color = null;
        for (var me : config.getLeftMinutesColors().entrySet())
            if (leftMinutes <= me.getKey()) {
                color = me.getValue();
                break;
            }

        return Log.format("<div style=\"{}\">{} MIN</div>",
            color != null ? "font-weight: bold; background-color: " + color + ";" : "",
            leftMinutes);
    }
}
