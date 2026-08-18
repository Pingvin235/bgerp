package org.bgerp.plugin.pln.callboard.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.cfg.Preferences;
import org.bgerp.app.exception.BGException;
import org.bgerp.plugin.pln.callboard.cache.CallboardCache;
import org.bgerp.util.Log;

import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;

public class WorkTypeTime {
    private static final Log log = Log.getLog();

    private boolean isDynamic;
    private int workTypeId;
    // minutes of the day when the work type starts and ends
    private int dayMinuteFrom;
    private int dayMinuteTo;
    // the work type entirely falls on the next day
    private boolean inNextDay;
    private String comment = "";

    public WorkTypeTime() {}

    public WorkTypeTime(boolean isDynamic, int workTypeId, int timeFrom, int timeTo, String comment) {
        this.workTypeId = workTypeId;
        this.dayMinuteFrom = timeFrom;
        this.dayMinuteTo = timeTo;
        this.comment = comment;
        this.isDynamic = isDynamic;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public int getWorkTypeId() {
        return workTypeId;
    }

    public void setWorkTypeId(int workTypeId) {
        this.workTypeId = workTypeId;
    }

    public int getDayMinuteFrom() {
        return dayMinuteFrom;
    }

    public void setDayMinuteFrom(int timeFrom) {
        this.dayMinuteFrom = timeFrom;
    }

    public int getDayMinuteTo() {
        return dayMinuteTo;
    }

    public void setDayMinuteTo(int timeTo) {
        this.dayMinuteTo = timeTo;
    }

    public static final String minutesToHourMin(int minutes) {
        return String.format("%02d:%02d", minutes / 60, minutes % 60);
    }

    public String getFormatedTimeFrom() {
        return minutesToHourMin(dayMinuteFrom);
    }

    public String getFormatedTimeTo() {
        return minutesToHourMin(dayMinuteTo);
    }

    public boolean isDynamic() {
        return isDynamic;
    }

    public boolean getIsDynamic() {
        return isDynamic;
    }

    public void setDynamic(boolean isDynamic) {
        this.isDynamic = isDynamic;
    }

    public static final List<WorkTypeTime> createFromString(String config) {
        List<WorkTypeTime> result = new ArrayList<>();
        Preferences setup = new Preferences(config);

        Map<Integer, ConfigMap> sortedMap = setup.subIndexed("rule.");
        for (ConfigMap entry : sortedMap.values()) {
            try {
                WorkTypeTime workTypeTime = new WorkTypeTime();
                workTypeTime.setWorkTypeId(entry.getInt("workTypeId", 0));
                workTypeTime.setDayMinuteFrom(getMinutes(entry.get("timeFrom", "")));
                workTypeTime.setDayMinuteTo(getMinutes(entry.get("timeTo", "")));
                workTypeTime.setDynamic(entry.getInt("isDynamic", 0) == 1);

                result.add(workTypeTime);
            } catch (BGException e) {
                log.error(e.getMessage(), e);
            }
        }

        setNextDays(result);

        return result;
    }

    public static final void setNextDays(List<WorkTypeTime> workTimes) {
        boolean inNextDay = false;

        for (WorkTypeTime workTypeTime : workTimes) {
            workTypeTime.inNextDay = inNextDay;

            if (!inNextDay && workTypeTime.dayMinuteTo < workTypeTime.dayMinuteFrom) {
                inNextDay = true;
            }
        }
    }

    private static final Pattern OLD_TIME_PATTERN = Pattern.compile("\\d{2}/\\d{2}/\\d{4}\\s+(\\d{2}):(\\d{2})");

    private static final int getMinutes(String value) {
        int result = Utils.parseInt(value, -1);

        // old format used when saving
        if (result < 0) {
            Matcher m = OLD_TIME_PATTERN.matcher(value);
            if (m.matches()) {
                result = Utils.parseInt(m.group(1)) * 60 + Utils.parseInt(m.group(2));
            } else {
                throw new BGException("Can't parse: " + value);
            }
        }

        return result;
    }

    public int getWorkMinutesInDay(WorkType type, Date dateStart, Date inDate) {
        return getMinutesInDay(type, dateStart, inDate, true);
    }

    public int getMinutesInDay(WorkType type, Date dateStart, Date inDate, boolean onlyWork) {
        if (type != null && (!onlyWork || !type.isNonWorkHours())) {
            Calendar inDateStart = TimeUtils.convertDateToCalendar(inDate);

            // all this complexity is caused by date shifting
            Calendar timeFrom = new GregorianCalendar();
            timeFrom.setTime(dateStart);
            timeFrom.add(Calendar.MINUTE, dayMinuteFrom);

            Calendar timeTo = new GregorianCalendar();
            timeTo.setTime(dateStart);
            timeTo.add(Calendar.MINUTE, dayMinuteTo);

            if (dayMinuteTo < dayMinuteFrom) {
                timeTo.add(Calendar.DAY_OF_YEAR, 1);
            } else if (inNextDay) {
                timeFrom.add(Calendar.DAY_OF_YEAR, 1);
                timeTo.add(Calendar.DAY_OF_YEAR, 1);
            }

            // only cases where our period's start or end falls into the date
            // for which we need to get the shift duration are considered
            if (inDate != null) {
                Calendar inDateEnd = TimeUtils.getNextDay(inDateStart);
                TimeUtils.clear_HOUR_MIN_MIL_SEC(inDateEnd);

                // no overlap
                if (inDateEnd.before(timeFrom) || timeTo.before(inDateStart)) {
                    return 0;
                }

                // truncating the range on the right and left
                if (timeFrom.before(inDateStart)) {
                    timeFrom = inDateStart;
                } else if (inDateEnd.before(timeTo)) {
                    timeTo = inDateEnd;
                }
            }

            // negative result
            return Math.max((int) ((timeTo.getTimeInMillis() - timeFrom.getTimeInMillis()) / 60000L), 0);
        }
        return 0;
    }

    public static int getWorkMinutesInDay(List<WorkTypeTime> workTypeTimeList, Date dateStart, Date inDate) {
        int result = 0;

        if (workTypeTimeList != null) {
            for (WorkTypeTime typeTime : workTypeTimeList) {
                result += typeTime.getWorkMinutesInDay(CallboardCache.getWorkType(typeTime.getWorkTypeId()), dateStart,
                        inDate);
            }
        }

        return result;
    }
}