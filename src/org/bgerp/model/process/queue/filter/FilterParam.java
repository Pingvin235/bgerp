package org.bgerp.model.process.queue.filter;

import java.util.Date;

import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.model.param.Parameter;

import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.TimeUtils;

public class FilterParam extends Filter {
    private Parameter parameter;

    public FilterParam(int id, ConfigMap filter, Parameter parameter) {
        super(id, filter);
        this.parameter = parameter;
    }

    public Parameter getParameter() {
        return parameter;
    }

    public void addDateTimeParamFilter(DynActionForm form, StringBuilder joinPart, String paramType) {
        final String paramPrefix = "dateTimeParam" + parameter.getId();

        final boolean orEmpty = configMap.getBoolean("orEmpty", false);

        Date dateFrom = form.getParamDate(paramPrefix + "From");
        Date dateTo = form.getParamDate(paramPrefix + "To");

        if (configMap.get("valueFrom", "").equals("curdate"))
            dateFrom = new Date();
        if (configMap.get("valueTo", "").equals("curdate"))
            dateTo = new Date();

        final String tableAlias = "param_dx_" + parameter.getId();

        if (dateFrom != null || dateTo != null || orEmpty) {
            joinPart.append(SQL_INNER_JOIN + "param_" + paramType + " AS " + tableAlias + " ON process.id=" + tableAlias + ".id AND " + tableAlias + ".param_id=" + parameter.getId());

            if (orEmpty)
                joinPart.append(" AND (" + tableAlias + ".param_id IS NULL OR (1>0 ");

            if (dateFrom != null)
                joinPart.append(" AND " + tableAlias + ".value>=" + TimeUtils.formatSqlDate(dateFrom));

            if (dateTo != null)
                joinPart.append(" AND " + tableAlias + ".value<" + TimeUtils.formatSqlDate(TimeUtils.getNextDay(dateTo)));

            if (orEmpty)
                joinPart.append("))");
        }
    }
}
