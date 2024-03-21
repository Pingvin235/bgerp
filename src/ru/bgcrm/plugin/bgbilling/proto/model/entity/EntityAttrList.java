package ru.bgcrm.plugin.bgbilling.proto.model.entity;

public class EntityAttrList extends EntityAttr {
    private int value;
    private String title;
    private String customValue;

    protected EntityAttrList() {
        super(EntitySpecAttrType.LIST);
    }

    public EntityAttrList(int entityId, int entitySpecAttrId, int value, String title) {
        super(EntitySpecAttrType.LIST, entityId, entitySpecAttrId);

        this.value = value;
        this.title = title;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCustomValue() {
        return customValue;
    }

    public void setCustomValue(String customValue) {
        this.customValue = customValue;
    }

    @Override
    public String toString() {
        return value < 1 ? customValue : title;
    }
}
