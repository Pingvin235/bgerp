package org.bgerp.plugin.clb.calendar.model;

import java.time.LocalDate;
import java.util.Map;

import org.bgerp.plugin.clb.calendar.Config;
import org.bgerp.util.Log;

import ru.bgcrm.dao.expression.Expression;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Setup;

/**
 * Event type calculated out of day.
 *
 * @author Shamil Vakhitov
 */
public class EventTypeRule {
    private static final Log log = Log.getLog();

    private final int id;
    private final String expression;
    private int typeId;

    public EventTypeRule(int id, ParameterMap config) {
        this.id = id;
        this.expression = config.get("check.expression");
        this.typeId = config.getInt("event.type");
    }

    public TimeType getEventType(LocalDate date) {
        if (new Expression(Map.of("date", date)).check(expression)) {
            log.debug("Found event type: {}, rule: {}", typeId, id);
            return Setup.getSetup().getConfig(Config.class).getEventType(typeId);
        }
        return null;
    }
}
