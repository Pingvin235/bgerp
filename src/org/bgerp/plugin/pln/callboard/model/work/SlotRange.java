package org.bgerp.plugin.pln.callboard.model.work;

import org.bgerp.plugin.pln.callboard.model.WorkTask;

public class SlotRange {
    public final WorkTask task;
    // с какого слота
    public final int slotFrom;
    // сколько занимают
    int slotCount;

    SlotRange(WorkTask task, int slotFrom, int slotCount) {
        this.task = task;
        this.slotFrom = slotFrom;
        this.slotCount = slotCount;
    }

    public WorkTask getTask() {
        return task;
    }

    public int getSlotFrom() {
        return slotFrom;
    }

    public int getSlotCount() {
        return slotCount;
    }
}