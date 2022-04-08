package ru.bgcrm.model.work.config;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import ru.bgcrm.model.work.WorkDaysCalendar;
import ru.bgcrm.model.work.WorkDaysCalendarRule;
import ru.bgcrm.util.Config;
import ru.bgcrm.util.ParameterMap;

public class CalendarConfig extends Config {
    private final Map<Integer, WorkDaysCalendar> calendarMap = new LinkedHashMap<Integer, WorkDaysCalendar>();

    public CalendarConfig(ParameterMap config) {
        super(null);

        for (Entry<Integer, ParameterMap> me : config.subIndexed("callboard.workdays.calendar.").entrySet()) {
            int id = me.getKey();
            ParameterMap pm = me.getValue();

            calendarMap.put(id, new WorkDaysCalendar(id, pm.get("title", ""), pm.get("comment", ""),
                    WorkDaysCalendarRule.createFromString(pm.get("rule", ""))));
        }
    }

    public Collection<WorkDaysCalendar> getCalendars() {
        return calendarMap.values();
    }

    public WorkDaysCalendar getCalendar(int id) {
        return calendarMap.get(id);
    }
}
