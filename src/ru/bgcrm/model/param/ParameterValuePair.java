package ru.bgcrm.model.param;

import static ru.bgcrm.model.param.Parameter.TYPE_ADDRESS;
import static ru.bgcrm.model.param.Parameter.TYPE_DATE;
import static ru.bgcrm.model.param.Parameter.TYPE_DATETIME;
import static ru.bgcrm.model.param.Parameter.TYPE_EMAIL;
import static ru.bgcrm.model.param.Parameter.TYPE_FILE;
import static ru.bgcrm.model.param.Parameter.TYPE_LIST;

import java.io.FileInputStream;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import ru.bgcrm.dao.FileDataDAO;
import ru.bgcrm.model.FileData;
import ru.bgcrm.model.IdTitle;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;

/**
 * Parameter value.
 *
 * @author Shamil Vakhitov
 */
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

    /**
     * @return object representation depending on parameter's type.
     */
    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public void setValueTitle(String valueTitle) {
        this.valueTitle = valueTitle;
    }

    /**
     * @return display parameter representation.
     */
    public String getValueTitle() {
        String result = valueTitle;
        if (result == null) {
            result = getValueTitle(parameter, value);
        }
        return result;
    }

    /**
     * Provides {@code src} attribute for HTML {@code img} tag in form of {@code data:image/png;base64, iVBORw0KGgoAAAANSUhEUgAAAAUA...===}.
     * @return encoded string or {@code null} if parameter value is also {@code null}.
     * @throws IllegalArgumentException when parameter type is not 'file'.
     */
    @SuppressWarnings("unchecked")
    public String getBase64EncodedImgSrc() throws Exception {
        if (value == null)
            return null;

        if (!TYPE_FILE.equals(parameter.getType()))
            throw new IllegalArgumentException("Attempt to get");

        FileData fileData = Utils.getFirst(((Map<Integer, FileData>) value).values());
        if (fileData == null)
            return null;

        var result = new StringBuilder(100000)
            .append("data:image/")
            .append(StringUtils.substringAfterLast(fileData.getTitle(), "."))
            .append(";base64, ");

        try (var fis = new FileInputStream(new FileDataDAO(null).getFile(fileData))) {
            byte[] data = IOUtils.toByteArray(fis);
            result.append(Base64.getEncoder().encodeToString(data));
        }

        return result.toString();
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
