package org.bgerp.plugin.calendar.model.event.processor;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

import org.bgerp.plugin.calendar.model.Minutes;
import org.bgerp.plugin.calendar.model.event.Event;
import org.bgerp.plugin.calendar.model.event.EventProcessor;

import ru.bgcrm.util.ParameterMap;

public class WorkingWeek40 extends EventProcessor {
    private final long minuteFrom;
    private final long minuteTo;

    protected WorkingWeek40(ParameterMap config) {
        super(null);
        minuteFrom = Minutes.timeFrom(config, "09:00");
        minuteTo = Minutes.timeTo(config, "18:00");
    }

    @Override
    public List<Event> process(List<Event> events) {
        var result = new ArrayList<Event>(events.size());

        for (var event : events) {
            result.add(event);

            var dw = event.getDate().getDayOfWeek();
            if (dw == DayOfWeek.SATURDAY || dw == DayOfWeek.SUNDAY)
                continue;

            event
                .withMinuteFrom(minuteFrom)
                .withMinuteTo(minuteTo);
        }

        return result;
    }
}
