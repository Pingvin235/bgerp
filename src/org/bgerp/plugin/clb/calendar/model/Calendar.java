package org.bgerp.plugin.clb.calendar.model;

import java.time.LocalTime;

import org.bgerp.model.base.IdTitle;

import ru.bgcrm.util.ParameterMap;

/**
 * Calendar.
 *
 * @author Shamil Vakhitov
 */
public class Calendar extends IdTitle {
    private final Mode mode;
    // For WEEK mode
    /** Operation time unit in minutes.  */
    private final long unitMinutes;
    /** Day range. */
    private final LocalTime dayTimeFrom;
    private final LocalTime dayTimeTo;

    public Calendar(int id, ParameterMap config) {
        super(id, config.get("title", "???"));
        mode = Mode.WEEK;
        unitMinutes = ConfigParser.parse(config.get("unit.minutes", "60"));
        dayTimeFrom = ConfigParser.timeFrom(config, "09:00");
        dayTimeTo = ConfigParser.timeTo(config, "18:00");
    }

    public Mode getMode() {
        return mode;
    }

    public long getUnitMinutes() {
        return unitMinutes;
    }

    public LocalTime getDayTimeFrom() {
        return dayTimeFrom;
    }

    public LocalTime getDayTimeTo() {
        return dayTimeTo;
    }
}