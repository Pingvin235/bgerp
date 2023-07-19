package org.bgerp.plugin.clb.calendar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.plugin.clb.calendar.model.Calendar;
import org.bgerp.plugin.clb.calendar.model.event.EventType;

public class Config extends org.bgerp.app.cfg.Config {
    private final List<EventType> eventTypeList;
    private final Map<Integer, EventType> eventTypeMap;
    private final List<Calendar> calendarList;
    private final Map<Integer, Calendar> calendarMap;

    protected Config(ConfigMap config, boolean validate) {
        super(null);

        config = config.sub(Plugin.ID + ":");

        this.eventTypeList = loadEventTypeList(config);
        this.eventTypeMap = eventTypeList.stream().collect(Collectors.toMap(EventType::getId, t -> t));
        this.calendarList = loadCalendarList(config);
        this.calendarMap = calendarList.stream().collect(Collectors.toMap(Calendar::getId, c -> c));
    }

    private List<EventType> loadEventTypeList(ConfigMap config) {
        var result = new ArrayList<EventType>();

        for (var me : config.subIndexed("event.type.").entrySet())
            result.add(new EventType(me.getKey(), me.getValue()));

        Collections.sort(result, (t1, t2) -> t1.getTitle().compareTo(t2.getTitle()));

        return Collections.unmodifiableList(result);
    }

    /* private List<EventType> loadBalanceEventTypeList(ParameterMap config) {
        var result = new ArrayList<EventType>();

        for (int typeId : Utils.toIntegerList(config.get("balance.event.type.ids"))) {}

        return Collections.unmodifiableList(result);
    } */

    private List<Calendar> loadCalendarList(ConfigMap config) {
        var result = new ArrayList<Calendar>();

        for (var me : config.subIndexed("calendar.").entrySet())
            result.add(new Calendar(me.getKey(), me.getValue()));

        Collections.sort(result, (c1, c2) -> c1.getTitle().compareTo(c2.getTitle()));

        return Collections.unmodifiableList(result);
    }

    public List<Calendar> getCalendarList() {
        return calendarList;
    }

    public Map<Integer, Calendar> getCalendarMap() {
        return calendarMap;
    }

    public List<EventType> getEventTypeList() {
        return eventTypeList;
    }

    public Map<Integer, EventType> getEventTypeMap() {
        return eventTypeMap;
    }
}
