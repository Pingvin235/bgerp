package org.bgerp.plugin.pln.callboard.model.work;

import java.util.Calendar;
import java.util.Date;

import org.bgerp.plugin.pln.callboard.model.WorkTypeConfig;
import org.bgerp.util.Dynamic;

import ru.bgcrm.util.TimeUtils;

public class FreeSlotRange {
    private final Date date;
    public final int groupId;
    public final ShiftData shiftData;
    private final CellRange cellRange;
    public final int slotFrom;
    public final int duration;
    private final int dayMinuteFrom;

    public FreeSlotRange(Date date, int groupId, ShiftData shiftData, CellRange cellRange, int slotFrom, int duration) {
        this.date = date;
        this.groupId = groupId;
        this.shiftData = shiftData;
        this.cellRange = cellRange;
        this.slotFrom = slotFrom;
        this.duration = duration;
        this.dayMinuteFrom = getDayMinuteFrom();
    }

    private int getDayMinuteFrom() {
        int result = cellRange.workTypeTime.getDayMinuteFrom();

        if (cellRange.workType.getTimeSetMode() == WorkTypeConfig.MODE_TIME_ON_STEP) {
            result += slotFrom * cellRange.workType.getTimeSetStep();
        }

        return result;
    }

    @Dynamic
    public Date getTime() {
        Calendar cal = TimeUtils.convertDateToCalendar(date);
        cal.add(Calendar.MINUTE, dayMinuteFrom);
        return TimeUtils.convertCalendarToDate(cal);
    }

    @Dynamic
    public int getGroupId() {
        return groupId;
    }

    @Dynamic
    public ShiftData getShiftData() {
        return shiftData;
    }
}