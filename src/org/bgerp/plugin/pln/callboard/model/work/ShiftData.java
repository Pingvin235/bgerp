package org.bgerp.plugin.pln.callboard.model.work;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.bgerp.plugin.pln.callboard.cache.CallboardCache;
import org.bgerp.plugin.pln.callboard.model.WorkTask;
import org.bgerp.plugin.pln.callboard.model.WorkType;
import org.bgerp.plugin.pln.callboard.model.WorkTypeTime;
import org.bgerp.plugin.pln.callboard.model.config.CallboardPlanConfig;
import org.bgerp.util.Dynamic;
import org.bgerp.util.Log;

// дневная смена в какой-то группе
public class ShiftData {
    private static final Log log = Log.getLog();

    public int team;
    public int shiftId;
    public Set<Integer> userIds;
    public List<WorkTypeTime> workTypeTimeList;
    private List<WorkTask> taskList;

    public int getTeam() {
        return team;
    }

    public int getShiftId() {
        return shiftId;
    }

    @Dynamic
    public Set<Integer> getUserIds() {
        return userIds;
    }

    public List<WorkTypeTime> getWorkTypeTimeList() {
        return workTypeTimeList;
    }

    public List<WorkTask> getTaskList() {
        return taskList;
    }

    public void addTask(WorkTask task) {
        if (taskList == null) {
            taskList = new ArrayList<>();
        }
        taskList.add(task);
    }

    public List<CellRange> getCellRanges(CallboardPlanConfig planConfig) {
        // для каждого wtt указано сколько слотов он занимает, если не указано
        List<CellRange> result = new ArrayList<>();

        CellRange currentRange = null;

        Iterator<WorkTypeTime> iterator = workTypeTimeList.iterator();

        // WorkTypeTime приближающийся либо текущий (ПТ)
        WorkTypeTime currentWtt = null;

        final int timeTo = planConfig.getDayMinuteTo();

        for (int currentTime = planConfig.getDayMinuteFrom(); currentTime < timeTo; currentTime += planConfig.getDayMinuteStep()) {
            // нет ПТ вида работ диапазона либо он завершился
            if (currentWtt == null || currentWtt.getDayMinuteTo() <= currentTime) {
                currentWtt = iterator.hasNext() ? iterator.next() : null;
            }

            // нет ПТ вида работ либо не добрались до вида работ
            if (currentWtt == null || currentTime < currentWtt.getDayMinuteFrom()) {
                if (currentRange == null || currentRange.workTypeTime != null) {
                    result.add(currentRange = new CellRange(null, null, 1));
                } else {
                    currentRange.cells++;
                }
            }
            // нет текущего диапазона, либо он не соответствует текущему виду работ
            else if (currentRange == null || currentRange.workTypeTime != currentWtt) {
                WorkType workType = null;
                if (currentWtt != null) {
                    workType = CallboardCache.getWorkType(currentWtt.getWorkTypeId());
                }
                result.add(currentRange = new CellRange(workType, currentWtt, 1));
            }
            // продление диапазона
            else {
                currentRange.cells++;
            }

        }

        // разброс тасков по диапазонам
        if (taskList != null) {
            for (WorkTask task : taskList) {
                int minuteFrom = task.getMinuteFrom();
                for (CellRange range : result) {
                    // пустое время
                    if (range.workTypeTime == null) {
                        continue;
                    }

                    int workTypeId = range.workTypeTime.getWorkTypeId();

                    WorkType workType = CallboardCache.getWorkType(workTypeId);
                    if (workType == null) {
                        log.warn("Not found WorkType with id: {}", workTypeId);
                        continue;
                    }

                    // попадает в диапазон - далее размещение по позиции
                    if (range.workTypeTime.getDayMinuteFrom() <= minuteFrom && minuteFrom < range.workTypeTime.getDayMinuteTo()) {
                        range.taskList.add(task);
                        task.setSlotTo(task.getSlotFrom() + (task.getDuration() / workType.getTimeSetStep()));
                    }
                }
            }
        }
        return result;
    }
}