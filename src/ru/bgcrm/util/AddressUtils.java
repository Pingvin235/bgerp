package ru.bgcrm.util;

import static ru.bgcrm.util.PatternFormatter.processPattern;

import java.sql.Connection;
import java.sql.SQLException;

import ru.bgcrm.dao.AddressDAO;
import ru.bgcrm.model.param.ParameterAddressValue;
import ru.bgcrm.model.param.address.AddressCity;
import ru.bgcrm.model.param.address.AddressHouse;
import ru.bgcrm.model.param.address.AddressItem;
import ru.bgcrm.util.PatternFormatter.PatternItemProcessor;

public class AddressUtils {
    private final static String ADDRESS_FORMAT = "(${index})(, ${city})(, ${area})(, ${quarter})(, ${street})(, д. ${house})(, кв. ${flat})( ${room})(, ${pod} под.)(, ${floor} эт.)( [${comment}])";

    public static final String buildAddressValue(final ParameterAddressValue value, Connection con) throws SQLException {
        return buildAddressValue(value, con, null);
    }

    public static final String buildAddressValue(final ParameterAddressValue value, Connection con, String formatName) throws SQLException {
        int houseId = value.getHouseId();

        AddressDAO addressDAO = new AddressDAO(con);
        AddressHouse house = value.getHouse();
        if (house == null) {
            house = addressDAO.getAddressHouse(houseId, true, true, true);
            if (house == null) {
                return "НЕ НАЙДЕН ДОМ С КОДОМ: " + houseId;
            }
        }

        final AddressHouse addressHouse = house;
        final AddressItem addressStreet = house.getAddressStreet();
        final AddressCity addressCity = addressStreet.getAddressCity();

        String address = Setup.getSetup().get("address.format", ADDRESS_FORMAT);
        if (Utils.notBlankString(formatName)) {
            address = Setup.getSetup().get("address.format." + formatName, address);
        }

        String result = processPattern(address, new PatternItemProcessor() {
            @Override
            public String processPatternItem(String variable) {
                if ("index".equals(variable)) {
                    return "";
                } else if ("city".equals(variable)) {
                    return addressCity.getTitle();
                } else if ("area".equals(variable)) {
                    return addressHouse.getAddressArea().getTitle();
                } else if ("quarter".equals(variable)) {
                    return addressHouse.getAddressQuarter().getTitle();
                } else if ("street".equals(variable)) {
                    return addressStreet.getTitle();
                } else if ("house".equals(variable)) {
                    return addressHouse.getHouseAndFrac();
                } else if ("flat".equals(variable)) {
                    return value.getFlat();
                } else if ("room".equals(variable)) {
                    return value.getRoom();
                } else if ("pod".equals(variable)) {
                    return value.getPod() > 0 ? String.valueOf(value.getPod()) : "";
                } else if ("floor".equals(variable)) {
                    return value.getFloor() > 0 ? String.valueOf(value.getFloor()) : "";
                } else if ("comment".equals(variable)) {
                    return value.getComment();
                }

                return "";
            }
        });

        return result;
    }

    /*public static final String buildAddressJEXL( ParameterAddressValue address, String pattern )
    {
        JexlContext context = new MapContext();
    
        context.set( "city", address.getHouse().getAddressStreet().getAddressCity().getTitle() );
    
        context.set( "street", address.getHouse().getAddressStreet().getTitle() );
    
        context.set( "area", address.getHouse().getAddressArea().getTitle() );
        context.set( "quarter", address.getHouse().getAddressQuarter().getTitle() );
    
        context.set( "house", address.getHouse().getHouse() );
        context.set( "frac", address.getHouse().getFrac() );
        context.set( "houseNfrac", address.getHouse().getHouseAndFrac() );
    
        context.set( "pod", address.getPod() );
        context.set( "floor", address.getFloor() );
    
        context.set( "flat", address.getFlat() );
        context.set( "room", address.getRoom() );
    
        JexlEngine jexl = new JexlEngine();
        Expression expression = jexl.createExpression( pattern );
    
        return (String)expression.evaluate( context );
    }*/

    public static String getHouseFlat(String value) {
        StringBuffer buf = new StringBuffer();
        if (value != null) {
            for (int index = 0; index < value.length(); index++) {
                char ch = value.charAt(index);
                if (Character.isDigit(ch)) {
                    buf.append(ch);
                } else {
                    break;
                }
            }
        }
        return buf.toString();
    }

    public static String getHouseRoom(String value) {
        StringBuffer buf = new StringBuffer();
        if (value != null) {
            for (int index = 0; index < value.length(); index++) {
                if (!Character.isDigit(value.charAt(index))) {
                    buf.append(value.substring(index));
                    break;
                }
            }
        }
        return buf.toString();
    }
}