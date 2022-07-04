package org.bgerp.plugin.clb.calendar.model;

import java.time.Duration;
import java.time.LocalTime;

import ru.bgcrm.util.ParameterMap;

public class Minutes {
    public static long parse(String value) {
        return Duration.between(LocalTime.MIN, LocalTime.parse(value)).toMinutes();
    }

    public static long timeFrom(ParameterMap config, String defaultValue) {
        return Minutes.parse(config.get("time.from", defaultValue));
    }

    public static long timeTo(ParameterMap config, String defaultValue) {
        return Minutes.parse(config.get("time.to", defaultValue));
    }
}
