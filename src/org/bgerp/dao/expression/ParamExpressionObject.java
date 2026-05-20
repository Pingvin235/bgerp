package org.bgerp.dao.expression;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.bgerp.cache.ParameterCache;
import org.bgerp.dao.param.ParamValueDAO;
import org.bgerp.model.base.IdTitle;
import org.bgerp.model.file.FileData;
import org.bgerp.model.param.Parameter;
import org.bgerp.util.Log;

import ru.bgcrm.model.param.ParameterAddressValue;
import ru.bgcrm.model.param.ParameterEmailValue;
import ru.bgcrm.model.param.ParameterPhoneValue;
import ru.bgcrm.model.param.ParameterPhoneValueItem;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;

/**
 * Expression object for retrieving object parameter values
 *
 * @author Shamil Vakhitov
 */
public class ParamExpressionObject implements ExpressionObject {
    private static final Log log = Log.getLog();

    private final ParamValueDAO paramDao;
    private final int objectId;

    public ParamExpressionObject(Connection con, int objectId) {
        this.paramDao = new ParamValueDAO(con);
        this.objectId = objectId;
    }

    @Override
    public void toContext(Map<String, Object> context) {
        throw new UnsupportedOperationException("The expression object can't be added to context");
    }

    /**
     * Select address parameter values strings
     * @param paramId the parameter ID
     * @param formatName optional address format, {@code null} for using the default one
     * @return list of address strings
     */
    public List<String> addressValues(int paramId, String formatName) {
        List<String> result = new ArrayList<>();

        Collection<ParameterAddressValue> value = getParamAddressValues(paramId, formatName);
        for (ParameterAddressValue addr : value) {
            result.add(addr.getValue());
        }

        return result;
    }

    /**
     * Select set with city IDs of parameter with type {@code address}
     * @param paramId the parameter ID
     * @return not {@code null} set of values
     */
    public Set<Integer> addressCityIds(int paramId) {
        Collection<ParameterAddressValue> value = getParamAddressValues(paramId, null);

        Set<Integer> result = new HashSet<>(value.size());
        for (ParameterAddressValue addr : value) {
            result.add(addr.getHouse().getAddressStreet().getCityId());
        }

        return result;
    }

    /**
     * Select set with street IDs of parameter with type {@code address}
     * @param paramId the parameter ID
     * @return not {@code null} set of values
     */
    public Set<Integer> addressStreetIds(int paramId) {
        Collection<ParameterAddressValue> value = getParamAddressValues(paramId, null);

        Set<Integer> result = new HashSet<>(value.size());
        for (ParameterAddressValue addr : value) {
            result.add(addr.getHouse().getStreetId());
        }

        return result;
    }

    /**
     * Select set with quarter IDs of parameter with type {@code address}
     * @param paramId the parameter ID
     * @return not {@code null} set of values
     */
    public Set<Integer> addressQuarterIds(int paramId) {
        Collection<ParameterAddressValue> value = getParamAddressValues(paramId, null);

        Set<Integer> result = new HashSet<>(value.size());
        for (ParameterAddressValue addr : value) {
            result.add(addr.getHouse().getQuarterId());
        }

        return result;
    }

    /**
     * Select set with area IDs of parameter with type {@code address}
     * @param paramId the parameter ID
     * @return not {@code null} set of values
     */
    public Set<Integer> addressAreaIds(int paramId) {
        Collection<ParameterAddressValue> value = getParamAddressValues(paramId, null);

        Set<Integer> result = new HashSet<>(value.size());
        for (ParameterAddressValue addr : value) {
            result.add(addr.getHouse().getAreaId());
        }

        return result;
    }

    protected Collection<ParameterAddressValue> getParamAddressValues(int paramId, String formatName) {
        Collection<ParameterAddressValue> value = Collections.emptyList();

        try {
            Parameter param = ParameterCache.getParameter(paramId);
            if (param == null || !Parameter.TYPE_ADDRESS.equals(param.getType())) {
                log.error("Param not found {} or not address in expression", paramId);
                return Collections.emptySet();
            }

            value = paramDao.getParamAddress(objectId, param.getId(), true, formatName).values();
        } catch (SQLException e) {
            log.error(e);
        }

        return value;
    }

    /**
     * Select value IDs of {@code list} and {@code listcount} parameter types
     * @param paramId the parameter ID
     * @return not {@code null} set of values
     */
    public Set<Integer> listValueIds(int paramId) {
        Set<Integer> result = Set.of();

        try {
            Parameter param = ParameterCache.getParameter(paramId);
            if (param == null || (!Parameter.TYPE_LIST.equals(param.getType()) && !Parameter.TYPE_LISTCOUNT.equals(param.getType()))) {
                log.error("Param not found with ID: " + paramId + " or has no 'list'/'listcount' type");
                return result;
            }

            result = Parameter.TYPE_LIST.equals(param.getType()) ? paramDao.getParamList(objectId, paramId) : paramDao.getParamListCount(objectId, paramId).keySet();
        } catch (SQLException e) {
            log.error(e);
        }

        return result;
    }

    /**
     * Select parameter values for the current object
     * @param paramId the parameter ID
     * @param format optional format of the result:<br>
     * <br>{@code nf} for param type {@code phone} returns {@link ParameterTypePhone} object
     * @return parameter's value, string if no {@param format} is defined
     */
    public Object val(int paramId, String format) {
        try {
            Parameter param = ParameterCache.getParameter(paramId);
            switch (Parameter.Type.of(param.getType())) {
                case ADDRESS -> {
                    return paramDao.getParamAddress(objectId, paramId).values().stream()
                        .map(ParameterAddressValue::getValue)
                        .collect(Collectors.joining("; "));
                }
                case BLOB -> {
                    return Utils.maskNull(paramDao.getParamBlob(objectId, paramId));
                }
                case DATE -> {
                    return TimeUtils.format(paramDao.getParamDate(objectId, paramId), param.getDateParamFormat());
                }
                case DATETIME -> {
                    return TimeUtils.format(paramDao.getParamDateTime(objectId, paramId), param.getDateParamFormat());
                }
                case EMAIL -> {
                    return ParameterEmailValue.toString(paramDao.getParamEmail(objectId, paramId).values());
                }
                case FILE -> {
                    return Utils.toString(paramDao.getParamFile(objectId, paramId).values().stream().map(FileData::getTitle).toList());
                }
                case LIST -> {
                    return Parameter.Type.listToString(paramId, paramDao.getParamListWithComments(objectId, paramId));
                }
                case LISTCOUNT -> {
                    return Parameter.Type.listCountToString(paramId, paramDao.getParamListCount(objectId, paramId));
                }
                case MONEY -> {
                    return Utils.format(paramDao.getParamMoney(objectId, paramId));
                }
                case PHONE -> {
                    ParameterPhoneValue value = paramDao.getParamPhone(objectId, paramId);
                    if ("nf".equals(format))
                        return value;
                    return value != null ? value.toString() : "";
                }
                case TEXT -> {
                    return Utils.maskNull(paramDao.getParamText(objectId, paramId));
                }
                case TREE -> {
                    return Parameter.Type.treeToString(paramId, paramDao.getParamTree(objectId, paramId));
                }
                case TREECOUNT -> {
                    return Parameter.Type.treeCountToString(paramId, paramDao.getParamTreeCount(objectId, paramId));
                }
            }
        } catch (SQLException e) {
            log.error(e);
            return e.getMessage();
        }

        return "";
    }

    /**
     * Select parameter values for the current object
     * @param paramId the parameter ID
     * @return string representation of the parameter value
     */
    public String val(int paramId) {
        return (String) val(paramId, null);
    }

    /**
     * Select parameter values for the current object
     * @param paramId the parameter ID
     * @return string representation of the parameter value
     */
    public String getValue(int paramId) {
        return val(paramId);
    }

    /**
     * Set parameter value(s) out of a string representation, the following parameter types are not supported:
     * {@link Parameter.Type#ADDRESS}, {@link Parameter.Type#FILE}, {@link Parameter.Type#LIST}, {@link Parameter.Type#LISTCOUNT},
     * {@link Parameter.Type#TREE}, {@link Parameter.Type#TREECOUNT}<br>
     * The {@code value} treated as:<br>
     * - {@link Parameter.Type#BLOB} - string value<br>
     * - {@link Parameter.Type#DATE} - formatted date string<br>
     * - {@link Parameter.Type#DATETIME} - formatted datetime string<br>
     * - {@link Parameter.Type#EMAIL} - comma-separated email addresses with possible display names<br>
     * - {@link Parameter.Type#MONEY} - dot-separated decimal number<br>
     * - {@link Parameter.Type#PHONE} - phone-separated phone numbers without format, only E164 digits<br>
     * - {@link Parameter.Type#TEXT} - string value<br>
     * @param paramId the parameter ID
     * @param value the string representation
     * @throws SQLException
     */
    public void sval(int paramId, String value) throws SQLException {
        Parameter param = ParameterCache.getParameter(paramId);
        switch (Parameter.Type.of(param.getType())) {
            case ADDRESS, FILE, LIST, LISTCOUNT, TREE, TREECOUNT -> {
                throw new UnsupportedOperationException();
            }
            case BLOB -> {
                paramDao.updateParamBlob(objectId, paramId, value);
            }
            case DATE -> {
                paramDao.updateParamDate(objectId, paramId, TimeUtils.parse(value, param.getDateParamFormat()));
            }
            case DATETIME -> {
                paramDao.updateParamDateTime(objectId, paramId, TimeUtils.parse(value, param.getDateParamFormat()));
            }
            case EMAIL -> {
                paramDao.updateParamEmail(objectId, paramId, ParameterEmailValue.of(value));
            }
            case MONEY -> {
                paramDao.updateParamMoney(objectId, paramId, Utils.parseBigDecimal(value));
            }
            case PHONE -> {
                var phone = new ParameterPhoneValue();
                for (String token : Utils.toList(value))
                    phone.addItem(new ParameterPhoneValueItem(token, ""));

                paramDao.updateParamPhone(objectId, paramId, phone);
            }
            case TEXT -> {
                paramDao.updateParamText(objectId, paramId, value);
            }
        }
    }

    // DEPRECATED

    /**
     * Use {@link ParamValueDAO#getParamAddress(int, int)}
     */
    @Deprecated
    public List<String> addressValues(int paramId) {
        log.warndMethod("addressValues", "val");

        return addressValues(paramId, null);
    }

    /**
     * Use {@link #getValue(int)}
     */
    @Deprecated
    public Set<String> listValueTitles(int paramId) {
        log.warndMethod("listValueTitles", "val");

        Set<String> result = Collections.emptySet();

        try {
            Parameter param = ParameterCache.getParameter(paramId);
            if (param == null || !Parameter.TYPE_LIST.equals(param.getType())) {
                log.error("Param not found: " + paramId + " or not list in expression");
                return Collections.emptySet();
            }

            result = new HashSet<>();
            for (IdTitle value : paramDao.getParamListWithTitles(objectId, paramId)) {
                result.add(value.getTitle());
            }
        } catch (SQLException e) {
            log.error(e);
        }

        return result;
    }

    /**
     * Use {@link ParamValueDAO#getParamText(int, int)}
     */
    @Deprecated
    public String getParamText(int paramId) throws SQLException {
        log.warndMethod("getParamText", "val");
        return paramDao.getParamText(objectId, paramId);
    }

    /**
     * Use {@link ParamValueDAO#getParamDate(int, int)}.
     */
    @Deprecated
    public Date getParamDate(int paramId) throws SQLException {
        log.warndMethod("getParamDate", "val");
        return paramDao.getParamDate(objectId, paramId);
    }

    /**
     * Use {@link ParamValueDAO#getParamDateTime(int, int)}.
     */
    @Deprecated
    public Date getParamDateTime(int paramId) throws SQLException {
        log.warndMethod("getParamDateTime", "val");
        return paramDao.getParamDateTime(objectId, paramId);
    }

    /**
     * Use {@link ParamValueDAO#getParamPhone(int, int)}.
     */
    @Deprecated
    public String getParamPhoneNoFormat(int paramId) throws SQLException {
        log.warndMethod("getParamPhoneNoFormat", "ParamValueDAO.getParamPhone");
        ParameterPhoneValue value = paramDao.getParamPhone(objectId, paramId);
        if (!value.getItemList().isEmpty()) {
            return value.getItemList().get(0).getPhone();
        }
        return null;
    }
}
