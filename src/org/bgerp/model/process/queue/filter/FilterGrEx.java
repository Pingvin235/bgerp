package org.bgerp.model.process.queue.filter;

import org.bgerp.app.cfg.ConfigMap;

import ru.bgcrm.dao.process.QueueSelectParams;
import ru.bgcrm.dao.process.Tables;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;

public class FilterGrEx extends Filter {
    private int roleId;
    private Filter groupsFilter;
    private Filter executorsFilter;

    public FilterGrEx(int id, ConfigMap filter) {
        super(id, filter);

        this.roleId = filter.getInt("roleId", 0);
        this.groupsFilter = new Filter(id, filter.sub("groups."));
        this.executorsFilter = new Filter(id, filter.sub("executors."));
    }

    public int getRoleId() {
        return roleId;
    }

    public Filter getGroupsFilter() {
        return groupsFilter;
    }

    public Filter getExecutorsFilter() {
        return executorsFilter;
    }

    @Override
    public void apply(DynActionForm form, QueueSelectParams params) {
        StringBuilder joinPart = params.joinPart;
        StringBuilder wherePart = params.wherePart;

        String groupIds = Utils.toString(form.getParamValues("group" + roleId));
        if (Utils.isBlankString(groupIds) && getOnEmptyValues().size() > 0) {
            groupIds = Utils.toString(getOnEmptyValues());
        }

        String pgTableAlias = "pg_" + roleId;

        if (Utils.notBlankString(groupIds))
            joinPart.append(SQL_INNER_JOIN + Tables.TABLE_PROCESS_GROUP + "AS " + pgTableAlias + " ON process.id=" + pgTableAlias + ".process_id AND "
                    + pgTableAlias + ".group_id IN(").append(groupIds).append(") AND " + pgTableAlias + ".role_id=" + roleId);

        String executorIds = Utils.toString(form.getParamValuesStr("executor" + roleId)).replace("current", String.valueOf(form.getUserId()));

        if (Utils.notBlankString(executorIds)) {
            String tableAlias = "pe_" + roleId;
            boolean empty = executorIds.contains("empty");

            if (empty)
                joinPart.append(SQL_LEFT_JOIN);
            else
                joinPart.append(SQL_INNER_JOIN);

            joinPart.append(Tables.TABLE_PROCESS_EXECUTOR + "AS " + tableAlias + " ON process.id=" + tableAlias + ".process_id AND " + tableAlias
                    + ".role_id=" + roleId);

            if (Utils.notBlankString(groupIds))
                joinPart.append(" AND " + pgTableAlias + ".group_id=" + tableAlias + ".group_id");

            if (empty)
                wherePart.append(" AND " + tableAlias + ".user_id IS NULL ");
            else {
                joinPart.append(" AND " + tableAlias + ".user_id IN(");
                joinPart.append(executorIds);
                joinPart.append(") ");
            }
        }
    }
}
