package org.bgerp.plugin.pln.grpl.model;

import java.time.Duration;
import java.time.LocalTime;

public class SlotPlacement extends Slot {
    private final LocalTime timeTo;

    SlotPlacement(Cell cell, LocalTime time, LocalTime timeTo) {
        super(cell, null, time, Duration.between(time, timeTo));
        this.timeTo = timeTo;
    }

    public LocalTime getTimeTo() {
        return timeTo;
    }
}
