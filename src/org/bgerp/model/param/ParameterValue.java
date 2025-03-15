package org.bgerp.model.param;

import static org.bgerp.model.param.Parameter.TYPE_ADDRESS;
import static org.bgerp.model.param.Parameter.TYPE_DATE;
import static org.bgerp.model.param.Parameter.TYPE_DATETIME;
import static org.bgerp.model.param.Parameter.TYPE_EMAIL;
import static org.bgerp.model.param.Parameter.TYPE_FILE;
import static org.bgerp.model.param.Parameter.TYPE_LIST;
import static org.bgerp.model.param.Parameter.TYPE_LISTCOUNT;
import static org.bgerp.model.param.Parameter.TYPE_TEXT;
import static org.bgerp.model.param.Parameter.TYPE_TREE;
import static org.bgerp.model.param.Parameter.TYPE_TREECOUNT;

import java.io.FileInputStream;
import java.math.BigDecimal;
import java.net.URLConnection;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.apache.commons.io.IOUtils;
import org.bgerp.model.base.IdTitle;

import ru.bgcrm.dao.FileDataDAO;
import ru.bgcrm.model.FileData;
import ru.bgcrm.model.param.ParameterAddressValue;
import ru.bgcrm.model.param.ParameterEmailValue;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;

/**
 * Parameter value
 *
 * @author Shamil Vakhitov
 */
public class ParameterValue {
    private final Parameter parameter;
    private Object value;
    private String valueTitle;

    public ParameterValue(Parameter parameter) {
        this.parameter = parameter;
    }

    public Parameter getParameter() {
        return parameter;
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

    /**
     * @return human-readable parameter value, {@code null} if {@link #value} is {@code null}
     */
    @SuppressWarnings("unchecked")
    public String getValueTitle() {
        String result = valueTitle;
        if (result == null && value != null) {
            String type = parameter.getType();
            if (TYPE_ADDRESS.equals(type)) {
                StringBuilder address = new StringBuilder();
                for (ParameterAddressValue item : ((Map<Integer, ParameterAddressValue>) value).values()) {
                    Utils.addSeparated(address, "; ", item.getValue());
                }
                result = address.toString();
            } else if (TYPE_DATE.equals(type)) {
                result = TimeUtils.format((Date) value, TimeUtils.FORMAT_TYPE_YMD);
            } else if (TYPE_DATETIME.equals(type)) {
                result = TimeUtils.format((Date) value, TimeUtils.FORMAT_TYPE_YMDHMS);
            } else if (TYPE_EMAIL.equals(type)) {
                result = Parameter.Type.emailToString(((SortedMap<Integer, ParameterEmailValue>) value).values());
            } else if (TYPE_LIST.equals(type)) {
                result = Utils.getObjectTitles((Collection<IdTitle>) value);
            } else if (TYPE_LISTCOUNT.equals(type)) {
                result = Parameter.Type.listCountToString(parameter.getId(), (Map<Integer, BigDecimal>) value);
            } else if (TYPE_TREE.equals(type)) {
                result = Parameter.Type.treeToString(parameter.getId(), (Set<String>) value);
            } else if (TYPE_TREECOUNT.equals(type)) {
                result = Parameter.Type.treeCountToString(parameter.getId(), (Map<String, BigDecimal>) value);
            } else {
                result = String.valueOf(value);
                if (TYPE_TEXT.equals(type) && "hideProtocol".equals(parameter.getShowAsLink())) {
                    int pos = result.indexOf("//");
                    if (pos > 0)
                        result = result.substring(pos + 2);
                }
            }
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
            .append("data:")
            .append(URLConnection.guessContentTypeFromName(fileData.getTitle()))
            .append(";base64, ");

        try (var fis = new FileInputStream(new FileDataDAO(null).getFile(fileData))) {
            byte[] data = IOUtils.toByteArray(fis);
            result.append(Base64.getEncoder().encodeToString(data));
        }

        return result.toString();
    }
}
