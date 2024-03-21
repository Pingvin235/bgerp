package ru.bgcrm.plugin.bgbilling.proto.model.entity;

public abstract class EntityAttr {
    private EntitySpecAttrType type;
    private int entityId;
    private int entitySpecAttrId;

    public EntityAttr() {
    }

    protected EntityAttr(EntitySpecAttrType type) {
        this(type, 0, 0);
    }

    protected EntityAttr(EntitySpecAttrType type, int entityId, int entitySpecAttrId) {
        this.type = type;
        this.entityId = entityId;
        this.entitySpecAttrId = entitySpecAttrId;
    }

    public int getEntityId() {
        return entityId;
    }

    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }

    public int getEntitySpecAttrId() {
        return entitySpecAttrId;
    }

    public void setEntitySpecAttrId(int entitySpecAttrId) {
        this.entitySpecAttrId = entitySpecAttrId;
    }
}
