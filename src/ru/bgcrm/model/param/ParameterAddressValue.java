package ru.bgcrm.model.param;

import java.sql.Connection;
import java.sql.SQLException;

import org.bgerp.app.cfg.Setup;
import org.bgerp.util.text.PatternFormatter;

import ru.bgcrm.dao.AddressDAO;
import ru.bgcrm.model.param.address.AddressCity;
import ru.bgcrm.model.param.address.AddressHouse;
import ru.bgcrm.model.param.address.AddressItem;
import ru.bgcrm.util.Utils;

/**
 * Value of parameter with type address
 *
 * @author Shamil Vakhitov
 */
public class ParameterAddressValue {
    private int houseId = -1;
    private String flat = "";
    private String room = "";
    private int pod = -1;
    private Integer floor;
    private String comment = "";
    /** House for generation {@link #value} */
    private AddressHouse house;
    /** Formatted address */
    private String value;

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

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String formatValue(Connection con) throws SQLException {
        return formatValue(con, null);
    }

    public String formatValue(Connection con, String formatName) throws SQLException {
        if (house == null) {
            house = new AddressDAO(con).getAddressHouse(houseId, true, true, true);
            if (house == null)
                throw new IllegalArgumentException("House not found: " + houseId);
        }

        final AddressHouse addressHouse = house;
        final AddressItem addressStreet = house.getAddressStreet();
        final AddressCity addressCity = addressStreet.getAddressCity();

        String address = Setup.getSetup().get("address.format", "(${street})(, ${house})(, ${floor} floor)(, apt. ${flat})( ${room})( ${comment})( ${index})( ${city})( [${comment}])");
        if (Utils.notBlankString(formatName)) {
            address = Setup.getSetup().get("address.format." + formatName, address);
        }

        String result = PatternFormatter.processPattern(address, variable -> {
            if ("index".equals(variable))
                return addressHouse.getPostIndex();
            if ("city".equals(variable))
                return addressCity.getTitle();
            if ("area".equals(variable))
                return addressHouse.getAddressArea().getTitle();
            if ("quarter".equals(variable))
                return addressHouse.getAddressQuarter().getTitle();
            if ("street".equals(variable))
                return addressStreet.getTitle();
            if ("house".equals(variable))
                return addressHouse.getHouseAndFrac();
            if ("flat".equals(variable))
                return flat;
            if ("room".equals(variable))
                return room;
            if ("pod".equals(variable))
                return pod > 0 ? String.valueOf(pod) : "";
            if ("floor".equals(variable))
                return floor == null ? "" : String.valueOf(floor);
            if ("comment".equals(variable))
                return comment;
            return "";
        });

        return result;
    }

    @Override
    public String toString() {
        return value;
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
}
