package org.bgerp.plugin.pln.callboard.model.config;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bgerp.app.cfg.Config;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.plugin.pln.callboard.model.WorkDaysCalendar;
import org.bgerp.plugin.pln.callboard.model.WorkDaysCalendarRule;

public class CalendarConfig extends Config {
    private final Map<Integer, WorkDaysCalendar> calendarMap = new LinkedHashMap<>();

    public CalendarConfig(ConfigMap config) {
        super(null);

        for (Entry<Integer, ConfigMap> me : config.subIndexed("callboard.workdays.calendar.").entrySet()) {
            int id = me.getKey();
            ConfigMap pm = me.getValue();

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
