package ru.bgcrm.model.param;

import ru.bgcrm.model.IdTitle;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import static ru.bgcrm.model.param.Parameter.*;

public class ParameterValuePair {
    private Parameter parameter;
    private Object value;
    private String valueTitle;

    public ParameterValuePair(Parameter parameter) {
        this.parameter = parameter;
    }

    public Parameter getParameter() {
        return parameter;
    }

    public void setParameter(Parameter parameter) {
        this.parameter = parameter;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public void setValueTitle(String valueTitle) {
        this.valueTitle = valueTitle;
    }

    public String getValueTitle() {
        String result = valueTitle;
        if (result == null) {
            result = getValueTitle(parameter, value);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public static final String getValueTitle(Parameter parameter, Object value) {
        String result = null;

        if (value == null) {
            return result;
        }

        String type = parameter.getType();
        if (TYPE_EMAIL.equals(type)) {
            result = Utils.toString(((TreeMap<String, String>) value).values(), "", "; ");
        } else if (TYPE_LIST.equals(type)) {
            result = Utils.getObjectTitles((Collection<IdTitle>) value);
        } else if (TYPE_DATE.equals(type)) {
            result = TimeUtils.format((Date) value, TimeUtils.FORMAT_TYPE_YMD);
        } else if (TYPE_DATETIME.equals(type)) {
            result = TimeUtils.format((Date) value, TimeUtils.FORMAT_TYPE_YMDHMS);
        } else if (TYPE_ADDRESS.equals(type)) {
            StringBuilder address = new StringBuilder();
            for (ParameterAddressValue item : ((Map<Integer, ParameterAddressValue>) value).values()) {
                Utils.addSeparated(address, "; ", item.getValue());
            }
            result = address.toString();
        } else {
            result = value == null ? "" : String.valueOf(value);
        }

        return result;
    }
}
