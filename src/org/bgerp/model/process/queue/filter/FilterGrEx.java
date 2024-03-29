package org.bgerp.model.process.queue.filter;

import org.bgerp.app.cfg.ConfigMap;

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
}
