package org.bgerp.plugin.pln.grpl.model;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import org.bgerp.app.cfg.Config;
import org.bgerp.app.cfg.ConfigMap;

public class ShiftConfig extends Config {
    private final LocalTime from;
    private final LocalTime to;
    private final Duration duration;

    ShiftConfig(ConfigMap config) {
        super(null);
        config = config.sub("shift.");
        from = LocalTime.parse(config.get("from", "09:00"), DateTimeFormatter.ISO_LOCAL_TIME);
        to = LocalTime.parse(config.get("to", "18:00"), DateTimeFormatter.ISO_LOCAL_TIME);
        duration = Duration.between(from, to);
    }

    public LocalTime getFrom() {
        return from;
    }

    public LocalTime getTo() {
        return to;
    }

    public Duration getDuration() {
        return duration;
    }
}
