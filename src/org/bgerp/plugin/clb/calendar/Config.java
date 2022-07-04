package org.bgerp.plugin.clb.calendar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bgerp.plugin.clb.calendar.model.Calendar;
import org.bgerp.plugin.clb.calendar.model.TimeType;

import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Utils;

public class Config extends ru.bgcrm.util.Config {
    private final Map<Integer, TimeType> timeTypeMap;
    private final List<TimeType> balanceEventTypeList;
    private final List<Calendar> calendarList;

    protected Config(ParameterMap config, boolean validate) {
        super(null);

        config = config.sub(Plugin.ID + ":");

        this.timeTypeMap = loadTimeTypeMap(config);
        this.balanceEventTypeList = loadBalanceEventTypeList(config);
        this.calendarList = loadCalendarList(config);
    }

    private Map<Integer, TimeType> loadTimeTypeMap(ParameterMap config) {
        var result = new HashMap<Integer, TimeType>();

        for (var me : config.subIndexed("time.type.").entrySet()) {
            var type = new TimeType(me.getKey(), me.getValue());
            result.put(type.getId(), type);
        }

        return Collections.unmodifiableMap(result);
    }

    private List<TimeType> loadBalanceEventTypeList(ParameterMap config) {
        var result = new ArrayList<TimeType>();

        for (int typeId : Utils.toIntegerList(config.get("balance.event.type.ids"))) {

        }

        return Collections.unmodifiableList(result);
    }

    private List<Calendar> loadCalendarList(ParameterMap config) {
        var result = new ArrayList<Calendar>();

        for (var me : config.subIndexed("calendar.").entrySet()) {
            result.add(new Calendar(me.getKey(), me.getValue()));
        }

        Collections.sort(result, (c1, c2) -> c1.getTitle().compareTo(c2.getTitle()));

        return Collections.unmodifiableList(result);
    }

    public TimeType getEventType(int id) {
        return timeTypeMap.get(id);
    }

    public List<TimeType> getBalanceEventTypeList() {
        return balanceEventTypeList;
    }

    public List<Calendar> getCalendarList() {
        return calendarList;
    }
}
