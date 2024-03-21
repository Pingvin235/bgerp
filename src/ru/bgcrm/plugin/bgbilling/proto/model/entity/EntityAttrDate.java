package ru.bgcrm.plugin.bgbilling.proto.model.entity;

import java.util.Date;

public class EntityAttrDate extends EntityAttr {
    private Date value;

    protected EntityAttrDate() {
        super(EntitySpecAttrType.DATE);
    }

    public EntityAttrDate(int entityId, int entitySpecAttrId, Date date) {
        super(EntitySpecAttrType.DATE, entityId, entitySpecAttrId);
        this.value = date;
    }

    public Date getValue() {
        return value;
    }

    public void setValue(Date date) {
        this.value = date;
    }
}
