package ru.bgcrm.model.param;

import ru.bgcrm.model.param.address.AddressHouse;
import ru.bgcrm.util.Utils;

/**
 * Value of parameter with type address.
 *
 * @author Shamil Vakhitov
 */
public class ParameterAddressValue {
    private int houseId = -1;
    private String flat = "";
    private String room = "";
    private int pod = -1;
    private Integer floor;
    private String value;
    private String comment = "";
    private AddressHouse house;

    public int getHouseId() {
        return houseId;
    }

    public void setHouseId(int houseId) {
        this.houseId = houseId;
    }

    public ParameterAddressValue withHouseId(int houseId) {
        setHouseId(houseId);
        return this;
    }

    public String getFlat() {
        return flat;
    }

    public void setFlat(String flat) {
        this.flat = flat;
    }

    public ParameterAddressValue withFlat(String flat) {
        setFlat(flat);
        return this;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public int getPod() {
        return pod;
    }

    public void setPod(int pod) {
        this.pod = pod;
    }

    public Integer getFloor() {
        return floor;
    }

    public void setFloor(Integer value) {
        this.floor = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public AddressHouse getHouse() {
        return house;
    }

    public void setHouse(AddressHouse house) {
        this.house = house;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof ParameterAddressValue))
            return false;

        ParameterAddressValue addressValue = (ParameterAddressValue) object;

        if (houseId != addressValue.houseId)
            return false;
        if (!Utils.maskNull(flat).equals(Utils.maskNull(addressValue.flat)))
            return false;
        if (!Utils.maskNull(room).equals(Utils.maskNull(addressValue.room)))
            return false;

        return true;
    }

    @Override
    public String toString() {
        return "ParameterAddressValue: " + value;
    }
}
