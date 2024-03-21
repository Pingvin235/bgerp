package ru.bgcrm.plugin.bgbilling.proto.model.entity;


/**
 * Атрибут/параметр - адрес.
 *
 * @author amir
 */
public class EntityAttrAddress
        extends EntityAttr {
    /**
     * Id дома
     */
    private int houseId = -1;

    /**
     * подъезд
     */
    private int pod;

    /**
     * этаж
     */
    private int floor;

    /**
     * квартира
     */
    private String flat;

    /**
     * комната
     */
    private String room = "";

    /**
     * комментарий
     */
    private String comment;

    /**
     * Форматированная строка с адресом
     */
    private String title;

    /**
     * Тип форматирования адреса
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
     * Получение ID дома.
     *
     * @return
     */
    public int getHouseId() {
        return houseId;
    }

    /**
     * Установка ID дома.
     *
     * @param houseId
     */
    public void setHouseId(int houseId) {
        this.houseId = houseId;
    }

    /**
     * Получение квартиры.
     *
     * @return
     */
    public String getFlat() {
        return flat;
    }

    /**
     * Установка квартиры.
     *
     * @param flat
     */
    public void setFlat(String flat) {
        this.flat = flat;
    }

    /**
     * Получение комнаты.
     *
     * @return
     */
    public String getRoom() {
        return room;
    }

    /**
     * Установка комнаты.
     *
     * @param room
     */
    public void setRoom(String room) {
        this.room = room;
    }

    /**
     * Получение подъезда.
     *
     * @return
     */
    public int getPod() {
        return pod;
    }

    /**
     * Установка подъезда.
     *
     * @param pod
     */
    public void setPod(int pod) {
        this.pod = pod;
    }

    /**
     * Получение этажа.
     *
     * @return
     */
    public int getFloor() {
        return floor;
    }

    /**
     * Установка этажа.
     *
     * @param floor
     */
    public void setFloor(int floor) {
        this.floor = floor;
    }

    /**
     * Получение комментария.
     *
     * @return
     */
    public String getComment() {
        return comment;
    }

    /**
     * Установка комментария.
     *
     * @param comment
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * Получение строкового представления адреса.
     *
     * @return
     */
    public String getTitle() {
        return title;
    }

    /**
     * Установка строкового представления адреса.
     *
     * @param title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Получение ключа форматирования адреса в строковое представление.
     *
     * @return
     */
    public String getFormatKey() {
        return formatKey;
    }

    /**
     * Установка ключа форматирования адреса в строковое представление.
     *
     * @param formatKey
     */
    public void setFormatKey(String formatKey) {
        this.formatKey = formatKey;
    }

    @Override
    public String toString() {
        return title;
    }

}
