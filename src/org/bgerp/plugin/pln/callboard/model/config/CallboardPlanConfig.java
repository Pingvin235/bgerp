package org.bgerp.plugin.pln.callboard.model.config;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.bgerp.app.cfg.Config;
import org.bgerp.app.cfg.ConfigMap;

import ru.bgcrm.util.TimeUtils;

public class CallboardPlanConfig extends Config {
    // minutes from the start of the day - plan start
    private final int dayMinuteFrom;
    // minutes from the start of the day - plan end
    private final int dayMinuteTo;
    // time step in minutes
    private final int dayMinuteStep;

    public CallboardPlanConfig(ConfigMap setup) {
        super(null);

        this.dayMinuteFrom = setup.getInt("dayMinuteFrom", 0);
        this.dayMinuteTo = setup.getInt("dayMinuteTo", 0);
        this.dayMinuteStep = setup.getInt("dayMinuteStep", 60);
    }

    public int getDayMinuteFrom() {
        return dayMinuteFrom;
    }

    public int getDayMinuteTo() {
        return dayMinuteTo;
    }

    public int getDayMinuteStep() {
        return dayMinuteStep;
    }

    public Calendar getTimeFrom(Date date) {
        Calendar result = TimeUtils.convertDateToCalendar(date);
        result.add(Calendar.MINUTE, dayMinuteFrom);
        return result;
    }

    public Calendar getTimeTo(Date date) {
        Calendar result = TimeUtils.convertDateToCalendar(date);
        result.add(Calendar.MINUTE, dayMinuteTo);
        return result;
    }

    public List<Date> getDateTimes(Date date) {
        List<Date> result = new ArrayList<>();

        for (int minute = dayMinuteFrom; minute < dayMinuteTo; minute += dayMinuteStep) {
            Calendar time = TimeUtils.convertDateToCalendar(date);
            time.add(Calendar.MINUTE, minute);
            result.add(TimeUtils.convertCalendarToDate(time));
        }

        return result;
    }
}