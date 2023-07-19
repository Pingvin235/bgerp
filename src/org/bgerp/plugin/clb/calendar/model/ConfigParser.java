package org.bgerp.plugin.clb.calendar.model;

import java.time.Duration;
import java.time.LocalTime;

import org.bgerp.app.cfg.ConfigMap;

public class ConfigParser {
    public static long parse(String value) {
        return Duration.between(LocalTime.MIN, LocalTime.parse(value)).toMinutes();
    }

    public static LocalTime timeFrom(ConfigMap config, String defaultValue) {
        return LocalTime.parse(config.get("time.from", defaultValue));
    }

    public static LocalTime timeTo(ConfigMap config, String defaultValue) {
        return LocalTime.parse(config.get("time.to", defaultValue));
    }
}
