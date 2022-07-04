package org.bgerp.plugin.clb.calendar.model;

import ru.bgcrm.model.IdTitle;
import ru.bgcrm.util.ParameterMap;

/**
 * Calendar.
 *
 * @author Shamil Vakhitov
 */
public class Calendar extends IdTitle {
    /** Minimal time unit in minutes.  */
    private final long unitMinutes;
    private final long minuteFrom;
    private final long minuteTo;

    public Calendar(int id, ParameterMap config) {
        super(id, config.get("title", "???"));
        unitMinutes = Minutes.parse(config.get("unit.minutes", "60"));
        minuteFrom = Minutes.timeFrom(config, "09:00");
        minuteTo = Minutes.timeTo(config, "18:00");
    }
}