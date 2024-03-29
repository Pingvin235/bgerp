package org.bgerp.plugin.pln.callboard.model;

import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.model.base.IdTitle;

public class DayType extends IdTitle {
    private final String color;
    private final int workHours;
    private final boolean holiday;

    public DayType(int id, ConfigMap config) {
        super(id, config.get("title", ""));
        this.color = config.get("color", "");
        this.workHours = config.getInt("workHours", 0);
        this.holiday = config.getBoolean("holiday", false);
    }

    public String getColor() {
        return color;
    }

    public int getWorkHours() {
        return workHours;
    }

    public boolean isHoliday() {
        return holiday;
    }
}