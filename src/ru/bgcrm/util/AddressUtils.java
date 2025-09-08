package ru.bgcrm.util;

import java.sql.Connection;
import java.sql.SQLException;

import org.bgerp.app.cfg.Setup;
import org.bgerp.util.text.PatternFormatter;

import ru.bgcrm.dao.AddressDAO;
import ru.bgcrm.model.param.ParameterAddressValue;
import ru.bgcrm.model.param.address.AddressCity;
import ru.bgcrm.model.param.address.AddressHouse;
import ru.bgcrm.model.param.address.AddressItem;

public class AddressUtils {
    private final static String ADDRESS_FORMAT_DEFAULT = "(${street})(, ${house})(, ${floor} floor)(, apt. ${flat})( ${room})( ${comment})( ${index})( ${city})( [${comment}])";

    public static final String buildAddressValue(final ParameterAddressValue value, Connection con) throws SQLException {
        return buildAddressValue(value, con, null);
    }

    public static final String buildAddressValue(final ParameterAddressValue value, Connection con, String formatName) throws SQLException {
        int houseId = value.getHouseId();

        AddressDAO addressDAO = new AddressDAO(con);
        AddressHouse house = value.getHouse();
        if (house == null) {
            house = addressDAO.getAddressHouse(houseId, true, true, true);
            if (house == null)
                throw new IllegalArgumentException("House not found: " + houseId);
        }

        final AddressHouse addressHouse = house;
        final AddressItem addressStreet = house.getAddressStreet();
        final AddressCity addressCity = addressStreet.getAddressCity();

        String address = Setup.getSetup().get("address.format", ADDRESS_FORMAT_DEFAULT);
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
                return value.getFlat();
            if ("room".equals(variable))
                return value.getRoom();
            if ("pod".equals(variable))
                return value.getPod() > 0 ? String.valueOf(value.getPod()) : "";
            if ("floor".equals(variable))
                return value.getFloor() == null ? "" : String.valueOf(value.getFloor());
            if ("comment".equals(variable))
                return value.getComment();
            return "";
        });

        return result;
    }
}
