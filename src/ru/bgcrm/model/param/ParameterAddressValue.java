package ru.bgcrm.model.param;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.lang3.StringUtils;
import org.bgerp.app.cfg.Setup;
import org.bgerp.app.exception.BGException;
import org.bgerp.cache.ParameterCache;
import org.bgerp.dao.expression.ParamExpressionObject;
import org.bgerp.util.Log;
import org.bgerp.util.text.PatternFormatter;

import ru.bgcrm.dao.AddressDAO;
import ru.bgcrm.model.param.address.AddressHouse;
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
                throw new BGException("House not found: " + houseId);
        }

        String pattern = Setup.getSetup().get("address.format", "(${street})(, ${house})(, ${floor} floor)(, apt. ${flat})( ${room})( ${comment})( ${index})( ${city})( [${comment}])");
        if (Utils.notBlankString(formatName)) {
            pattern = Setup.getSetup().get("address.format." + formatName, pattern);
        }

        String result = PatternFormatter.processPattern(pattern, variable -> {
            if ("index".equals(variable))
                return house.getPostIndex();
            if ("city".equals(variable))
                return house.getAddressStreet().getAddressCity().getTitle();
            if ("area".equals(variable))
                return house.getAddressArea().getTitle();
            if ("quarter".equals(variable))
                return house.getAddressQuarter().getTitle();
            if ("street".equals(variable))
                return house.getAddressStreet().getTitle();
            if ("house".equals(variable))
                return house.getHouseAndFrac();
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
            if (variable.startsWith("param_")) {
                if (con == null)
                    throw new BGException("Can't use ${param_<ID>} variables with con=null");
                var p = ParameterCache.getParameter(Utils.parseInt(StringUtils.substringAfter(variable, "_")));
                if (p == null)
                    throw new BGException("Not found parameter for variable: " + variable);
                if (!AddressHouse.OBJECT_TYPE.equals(p.getObjectType()))
                    throw new BGException(Log.format("Object type for parameter '{}' must be '{}'", variable, AddressHouse.OBJECT_TYPE));

                return new ParamExpressionObject(con, houseId).val(p.getId());
            }
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
