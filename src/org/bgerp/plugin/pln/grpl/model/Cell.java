package org.bgerp.plugin.pln.grpl.model;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.bgerp.cache.UserCache;
import org.bgerp.util.Dynamic;

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

    @Dynamic
    public List<Group> getFreeGroups() {
        Set<Integer> excludedIds = row.excludeGroupIds();
        return row.getBoard().getGroupIds().stream()
            .filter(id -> !excludedIds.contains(id))
            .map(id -> UserCache.getUserGroup(id))
            .collect(Collectors.toList());
    }

    public void addSlotPlacements() {
        var shift = row.getBoard().getShift();

        var time = shift.getFrom();
        LocalTime timeTo = shift.getTo();

        int firstIndexNoTime = -1;

        for (int i = 0; i < slots.size(); i++) {
            var slot = slots.get(i);

            // duration is null?

            if (slot.getTime() == null) {
                timeTo = timeTo.minus(slot.getDuration());
                if (firstIndexNoTime < 0)
                    firstIndexNoTime = i;
                continue;
            }

            if (time.isBefore(slot.getTime()))
                slots.add(i++, new SlotPlacement(this, time, slot.getTime()));

            time = slot.getTime().plus(slot.getDuration());
        }

        if (time.isBefore(timeTo)) {
            var placement = new SlotPlacement(this, time, timeTo);
            if (firstIndexNoTime < 0)
                slots.add(placement);
            else
                slots.add(firstIndexNoTime, placement);
        }
    }

    public List<Slot> getSlots() {
        return slots;
    }
}
