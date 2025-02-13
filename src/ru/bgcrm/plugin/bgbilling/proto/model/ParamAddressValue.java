package ru.bgcrm.plugin.bgbilling.proto.model;

import java.sql.Connection;
import java.sql.SQLException;

import ru.bgcrm.model.param.ParameterAddressValue;
import ru.bgcrm.util.AddressUtils;
import ru.bgcrm.util.Utils;

/**
 * Значение адресного параметра в BGBilling
 */
public class ParamAddressValue {
    private int cityId;
    private String cityTitle = "";
    private String areaTitle = "";
    private String quarterTitle = "";
    private int streetId;
    private String streetTitle = "";
    private int houseId;
    private String house = "";
    private String pod = "";
    private String floor = "";
    private String index = "";
    private String flat = "";
    private String room = "";
    private String comment = "";

    public String getAreaTitle() {
        return areaTitle;
    }

    public void setAreaTitle(String areaTitle) {
        this.areaTitle = areaTitle;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public String getCityTitle() {
        return cityTitle;
    }

    public void setCityTitle(String cityTitle) {
        this.cityTitle = cityTitle;
    }

    public String getQuarterTitle() {
        return quarterTitle;
    }

    public void setQuarterTitle(String quarterTitle) {
        this.quarterTitle = quarterTitle;
    }

    public int getStreetId() {
        return streetId;
    }

    public void setStreetId(int streetId) {
        this.streetId = streetId;
    }

    public String getStreetTitle() {
        return streetTitle;
    }

    public void setStreetTitle(String streetTitle) {
        this.streetTitle = streetTitle;
    }

    public int getHouseId() {
        return houseId;
    }

    public void setHouseId(int houseId) {
        this.houseId = houseId;
    }

    public String getHouse() {
        return house;
    }

    public void setHouse(String house) {
        this.house = house;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getPod() {
        return pod;
    }

    public void setPod(String pod) {
        this.pod = pod;
    }

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    public String getFlat() {
        return flat;
    }

    public void setFlat(String flat) {
        this.flat = flat;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * Конвертирует объект в формат ERP.
     * @param con
     * @return
     * @throws SQLException
     */
    public ParameterAddressValue toParameterAddressValue(Connection con) throws SQLException {
        ParameterAddressValue result = new ParameterAddressValue();

        result.setComment(getComment());
        result.setFlat(getFlat());
        String floor = getFloor();
        if (Utils.notBlankString(floor))
            result.setFloor(Utils.parseInt(floor));
        result.setHouseId(getHouseId());
        result.setPod(Utils.parseInt(getPod()));
        result.setRoom(getRoom());
        if (result.getHouseId() != 0) {
            result.setValue(AddressUtils.buildAddressValue(result, con));
        } else {
            result.setValue("");
        }

        return result;
    }
}
