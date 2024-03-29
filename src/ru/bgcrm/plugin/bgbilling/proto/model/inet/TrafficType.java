package ru.bgcrm.plugin.bgbilling.proto.model.inet;

import org.bgerp.model.base.IdTitle;

import ru.bgcrm.plugin.bgbilling.proto.model.ServiceUnit;

/**
 * Тип трафика
 * @author amir
 *
 */
public class TrafficType extends IdTitle {
    public static final Integer TIME_ID = 0;
    public static final TrafficType TIME = new TrafficType(0, "Время", ServiceUnit.UNIT_SECONDS);

    /**
     * @see ServiceUnit
     */
    private int unit;

    public TrafficType() {
        super();
    }

    public TrafficType(int id, String title, int unit) {
        super(id, title);

        this.unit = unit;
    }

    /**
     * @return
     * @see ServiceUnit
     */
    public int getUnit() {
        return unit;
    }

    public void setUnit(int type) {
        this.unit = type;
    }
}
