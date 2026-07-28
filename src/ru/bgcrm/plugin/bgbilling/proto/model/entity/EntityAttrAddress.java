package ru.bgcrm.plugin.bgbilling.proto.model.entity;


/**
 * Attribute/parameter - address
 *
 * @author amir
 */
public class EntityAttrAddress
        extends EntityAttr {
    /**
     * House ID
     */
    private int houseId = -1;

    /**
     * Entrance
     */
    private int pod;

    /**
     * Floor
     */
    private int floor;

    /**
     * Flat
     */
    private String flat;

    /**
     * Room
     */
    private String room = "";

    /**
     * Comment
     */
    private String comment;

    /**
     * Formatted address string
     */
    private String title;

    /**
     * Address formatting type
     */
    private String formatKey;

    protected EntityAttrAddress() {
        super(EntitySpecAttrType.ADDRESS);
    }

    public EntityAttrAddress(int entityId, int entitySpecAttrId) {
        super(EntitySpecAttrType.ADDRESS, entityId, entitySpecAttrId);
    }

    public EntityAttrAddress(int entityId, int entitySpecAttrId, int houseId, int pod, int floor, String flat, String room, String comment, String formatKey, String title) {
        this(entityId, entitySpecAttrId);

        this.houseId = houseId;
        this.flat = flat;
        this.pod = pod;
        this.floor = floor;
        this.room = room;
        this.comment = comment;
        this.formatKey = formatKey;
        this.title = title;
    }

    /**
     * @return the house ID
     */
    public int getHouseId() {
        return houseId;
    }

    /**
     * @param houseId the house ID
     */
    public void setHouseId(int houseId) {
        this.houseId = houseId;
    }

    /**
     * @return the flat
     */
    public String getFlat() {
        return flat;
    }

    /**
     * @param flat the flat
     */
    public void setFlat(String flat) {
        this.flat = flat;
    }

    /**
     * @return the room
     */
    public String getRoom() {
        return room;
    }

    /**
     * @param room the room
     */
    public void setRoom(String room) {
        this.room = room;
    }

    /**
     * @return the entrance
     */
    public int getPod() {
        return pod;
    }

    /**
     * @param pod the entrance
     */
    public void setPod(int pod) {
        this.pod = pod;
    }

    /**
     * @return the floor
     */
    public int getFloor() {
        return floor;
    }

    /**
     * @param floor the floor
     */
    public void setFloor(int floor) {
        this.floor = floor;
    }

    /**
     * @return the comment
     */
    public String getComment() {
        return comment;
    }

    /**
     * @param comment the comment
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * @return the formatted address string
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the formatted address string
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the address formatting type
     */
    public String getFormatKey() {
        return formatKey;
    }

    /**
     * @param formatKey the address formatting type
     */
    public void setFormatKey(String formatKey) {
        this.formatKey = formatKey;
    }

    @Override
    public String toString() {
        return title;
    }

}
