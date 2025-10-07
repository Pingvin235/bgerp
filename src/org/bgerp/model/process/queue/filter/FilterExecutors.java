package org.bgerp.model.process.queue.filter;

import java.util.Collections;
import java.util.Set;

import org.bgerp.app.cfg.ConfigMap;

import ru.bgcrm.dao.process.QueueSelectParams;
import ru.bgcrm.dao.process.Tables;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;

public class FilterExecutors extends Filter {
    public FilterExecutors(int id, ConfigMap filter) {
        super(id, filter);
    }

    @Override
    public void apply(DynActionForm form, QueueSelectParams params) {
        StringBuilder joinPart = params.joinPart;
        StringBuilder wherePart = params.wherePart;

        Set<String> executorIds = null;

        // hard filter with only the current executor
        if (getValues().contains("current")) {
            executorIds = Collections.singleton(String.valueOf(form.getUserId()));
        } else {
            executorIds = form.getParamValuesStr("executor");
            if (executorIds.remove("current"))
                executorIds.add(String.valueOf(form.getUserId()));
        }

        if (executorIds.size() > 0) {
            joinPart.append(SQL_LEFT_JOIN + Tables.TABLE_PROCESS_EXECUTOR + "AS ie ON process.id=ie.process_id ");

            boolean empty = executorIds.remove("empty");
            // preventing IN ()
            if (empty)
                executorIds.add("-1");

            wherePart.append(" AND (ie.user_id IN (").append(Utils.toString(executorIds)).append(")");
            if (empty)
                wherePart.append(" OR ie.user_id IS NULL");
            wherePart.append(")");
        }
    }
}
