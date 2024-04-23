package org.bgerp.plugin.pln.callboard.model.work;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bgerp.plugin.pln.callboard.model.WorkTask;
import org.bgerp.plugin.pln.callboard.model.WorkType;
import org.bgerp.plugin.pln.callboard.model.WorkTypeTime;
import org.bgerp.util.Log;

// диапазон ячеек в смене, занятый типом работ либо свободный
public class CellRange {
    private static final Log log = Log.getLog();

    // null - свободное время
    public final WorkType workType;
    // null - свободное время
    public final WorkTypeTime workTypeTime;
    /*private final int startDayMinute;*/
    // сколько ячеек занимает в дневной таблице
    int cells;
    // на сколько слотов разбит
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

    // возвращает свободные слоты либо занятые каким-то процессом
    public List<SlotRange> getSlotRanges() {
        List<SlotRange> result = new ArrayList<>();

        if (workTypeTime != null) {
            Iterator<WorkTask> taskIterator = taskList.iterator();

            SlotRange currentRange = null;

            // WorkTask на подходе либо текущий
            WorkTask currentTask = null;

            for (int slot = 0; slot < slotCount; slot++) {
                if (currentTask == null || currentTask.getSlotTo() <= slot) {
                    currentTask = taskIterator.hasNext() ? taskIterator.next() : null;
                }

                // есть текущий диапазон и слот попал в него
                if (currentTask != null && (currentTask.getSlotFrom() <= slot && slot < currentTask.getSlotTo())) {
                    if (currentRange == null || currentRange.task != currentTask) {
                        result.add(currentRange = new SlotRange(currentTask, slot, 1));
                    } else {
                        currentRange.slotCount++;
                    }
                }
                // нет текущего диапазона, либо не дошли до него
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
     * Возвращает свободные диапазоны слотов.
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

            // слот не занят
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