package org.bgerp.plugin.pln.callboard.model.work;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bgerp.plugin.pln.callboard.model.WorkTask;
import org.bgerp.plugin.pln.callboard.model.WorkType;
import org.bgerp.plugin.pln.callboard.model.WorkTypeTime;
import org.bgerp.util.Log;

// range of cells in a shift, occupied by a work type or free
public class CellRange {
    private static final Log log = Log.getLog();

    // null - free time
    public final WorkType workType;
    // null - free time
    public final WorkTypeTime workTypeTime;
    /*private final int startDayMinute;*/
    // how many cells it occupies in the day table
    int cells;
    // into how many slots it is split
    private final int slotCount;
    List<WorkTask> taskList = new ArrayList<>();

    CellRange(WorkType workType, WorkTypeTime workTypeTime, int cells) {
        this.workType = workType;
        this.workTypeTime = workTypeTime;
        this.cells = cells;
        this.slotCount = workType != null ? workType.getSlotCount(workTypeTime.getDayMinuteFrom(), workTypeTime.getDayMinuteTo()) : 1;
    }

    public WorkType getWorkType() {
        return workType;
    }

    public WorkTypeTime getWorkTypeTime() {
        return workTypeTime;
    }

    public int getCells() {
        return cells;
    }

    // returns free slots or slots occupied by some process
    public List<SlotRange> getSlotRanges() {
        List<SlotRange> result = new ArrayList<>();

        if (workTypeTime != null) {
            Iterator<WorkTask> taskIterator = taskList.iterator();

            SlotRange currentRange = null;

            // WorkTask coming up or current
            WorkTask currentTask = null;

            for (int slot = 0; slot < slotCount; slot++) {
                if (currentTask == null || currentTask.getSlotTo() <= slot) {
                    currentTask = taskIterator.hasNext() ? taskIterator.next() : null;
                }

                // there is a current range and the slot falls into it
                if (currentTask != null && (currentTask.getSlotFrom() <= slot && slot < currentTask.getSlotTo())) {
                    if (currentRange == null || currentRange.task != currentTask) {
                        result.add(currentRange = new SlotRange(currentTask, slot, 1));
                    } else {
                        currentRange.slotCount++;
                    }
                }
                // no current range, or haven't reached it yet
                else {
                    result.add(new SlotRange(null, slot, 1));
                }
            }
        }

        return result;
    }

    public int getDayMinuteFrom(SlotRange slotRange) {
        return workTypeTime.getDayMinuteFrom() + slotRange.slotFrom * workType.getTimeSetStep();
    }

    /**
     * Returns free slot ranges
     * @return
     *
     * 11-22/16:03:30 DEBUG [http-bio-9089-exec-9] WorkAction - Checking free ranges:
    11-22/16:03:30 DEBUG [http-bio-9089-exec-9] WorkAction - SlotRange from: 0; count: 1; task: null
    11-22/16:03:30 DEBUG [http-bio-9089-exec-9] WorkAction - SlotRange from: 1; count: 1; task: null
    11-22/16:03:30 DEBUG [http-bio-9089-exec-9] WorkAction - SlotRange from: 2; count: 1; task: null
    11-22/16:03:30 DEBUG [http-bio-9089-exec-9] WorkAction - SlotRange from: 3; count: 1; task: null
    11-22/16:03:30 DEBUG [http-bio-9089-exec-9] WorkAction - SlotRange from: 4; count: 4; task: ru.bgcrm.model.work.WorkTask@b4114
    11-22/16:03:30 DEBUG [http-bio-9089-exec-9] WorkAction - SlotRange from: 8; count: 1; task: null
    11-22/16:03:30 DEBUG [http-bio-9089-exec-9] WorkAction - SlotRange from: 9; count: 1; task: null
    11-22/16:03:30 DEBUG [http-bio-9089-exec-9] WorkAction - SlotRange from: 10; count: 1; task: null
    11-22/16:03:30 DEBUG [http-bio-9089-exec-9] WorkAction - SlotRange from: 11; count: 1; task: null
    11-22/16:03:30 DEBUG [http-bio-9089-exec-9] WorkAction - SlotRange from: 12; count: 1; task: null
    11-22/16:03:30 DEBUG [http-bio-9089-exec-9] WorkAction - SlotRange from: 13; count: 1; task: null
    11-22/16:03:30 DEBUG [http-bio-9089-exec-9] WorkAction - Date: 22.11.2014; Shift team: 1; userIds: [3443]; shiftId: 165; time: 120; slotSize: 30
    11-22/16:03:30 DEBUG [http-bio-9089-exec-9] WorkAction - Checking free ranges: 0-4, 5-11
     */
    public List<int[]> getFreeSlotRanges() {
        List<int[]> rangeList = new ArrayList<>();

        int[] currentRange = null;

        for (SlotRange pair : getSlotRanges()) {
            log.debug("SlotRange from: {}; count: {} task: {}", pair.slotFrom, pair.slotCount, pair.task);

            // slot is not occupied
            if (pair.task != null) {
                currentRange = null;
            } else {
                if (currentRange == null) {
                    rangeList.add(currentRange = new int[] { pair.slotFrom, pair.slotFrom });
                } else {
                    currentRange[1] = pair.slotFrom;
                }
            }
        }

        return rangeList;
    }
}