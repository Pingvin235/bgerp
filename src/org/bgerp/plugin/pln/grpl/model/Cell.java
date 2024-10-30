package org.bgerp.plugin.pln.grpl.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.bgerp.cache.UserCache;

import ru.bgcrm.model.user.Group;

public class Cell {
    private final Row row;
    private final int columnId;
    private final Group group;
    private final List<Slot> slots;

    public Cell(Row row, int columnId, Group group) {
        this.row = row;
        this.columnId = columnId;
        this.group = group;
        this.slots = new ArrayList<>();
    }

    public Row getRow() {
        return row;
    }

    public int getColumnId() {
        return columnId;
    }

    public Group getGroup() {
        return group;
    }

    public List<Group> getFreeGroups() {
        Set<Integer> excludedIds = row.excludeGroupIds();
        return row.getBoard().getGroupIds().stream()
            .filter(id -> !excludedIds.contains(id))
            .map(id -> UserCache.getUserGroup(id))
            .collect(Collectors.toList());
    }

    public List<Slot> getSlots() {
        return slots;
    }
}
