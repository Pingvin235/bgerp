package ru.bgcrm.plugin.bgbilling.proto.model.entity;

public class EntityAttrText extends EntityAttr {
    private String value;

    protected EntityAttrText() {
        super(EntitySpecAttrType.TEXT);
    }

    public EntityAttrText(int entityId, int entitySpecAttrId, String value) {
        super(EntitySpecAttrType.TEXT, entityId, entitySpecAttrId);
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value != null ? value : "";
    }
}
