package org.bgerp.plugin.clb.calendar.model.event;

import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.model.base.IdTitle;

/**
 * Time type, e.g.: work, holyday, weekend.
 *
 * @author Shamil Vakhitov
 */
public class EventType extends IdTitle {
    private final String color;

    public EventType(int id, ConfigMap config) {
        super(id, config.get("title", "??? [" + id + "]"));
        this.color = config.get("color");
    }

    public String getColor() {
        return color;
    }
}
