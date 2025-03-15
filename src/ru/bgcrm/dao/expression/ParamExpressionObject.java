package ru.bgcrm.dao.expression;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.bgerp.cache.ParameterCache;
import org.bgerp.dao.param.ParamValueDAO;
import org.bgerp.model.base.IdTitle;
import org.bgerp.model.param.Parameter;
import org.bgerp.util.Log;

import ru.bgcrm.model.FileData;
import ru.bgcrm.model.param.ParameterAddressValue;
import ru.bgcrm.model.param.ParameterPhoneValue;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;

/**
 * Expression object for retrieving object parameter values
 *
 * @author Shamil Vakhitov
 */
public abstract class ParamExpressionObject implements ExpressionObject {
    private static final Log log = Log.getLog();

    private final ParamValueDAO paramDao;
    private final int objectId;

    protected ParamExpressionObject(Connection con, int objectId) {
        this.paramDao = new ParamValueDAO(con);
        this.objectId = objectId;
    }

    /**
     * Возвращает список строк со значениями адресного параметра, формат указан.
     * @param paramId the parameter ID
     * @return
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
     * Selects set with city IDs of parameter with type {@code address}
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
     * Selects set with street IDs of parameter with type {@code address}
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
     * Selects set with quarter IDs of parameter with type {@code address}
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
     * Selects set with area IDs of parameter with type {@code address}
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
     * Selects value IDs of {@code list} and {@code listcount} parameter types
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
     * Selects parameter values for the current object
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
                case PHONE -> {
                    ParameterPhoneValue value = paramDao.getParamPhone(objectId, paramId);
                    if ("nf".equals(format))
                        return value;
                    return value != null ? value.toString() : "";
                }
                case TEXT -> {
                    return Utils.maskNull(paramDao.getParamText(objectId, paramId));
                }
                case FILE -> {
                    return Utils.toString(paramDao.getParamFile(objectId, paramId).values().stream().map(FileData::getTitle).toList());
                }
                case EMAIL -> {
                    return Parameter.Type.emailToString(paramDao.getParamEmail(objectId, paramId).values());
                }
                case LIST -> {
                    return Utils.getObjectTitles(param.getListParamValues(), paramDao.getParamList(objectId, paramId));
                }
                case LISTCOUNT -> {
                    return Parameter.Type.listCountToString(paramId, paramDao.getParamListCount(objectId, paramId));
                }
                case MONEY -> {
                    return Utils.format(paramDao.getParamMoney(objectId, paramId));
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
     * Selects parameter values for the current object.
     * @param paramId the parameter ID.
     * @return string representation of the parameter value.
     */
    public String val(int paramId) {
        return (String) val(paramId, null);
    }

    /**
     * Selects parameter values for the current object.
     * @param paramId the parameter ID.
     * @return string representation of the parameter value.
     */
    public String getValue(int paramId) {
        return val(paramId);
    }

    // DEPRECATED

    /**
     * Use {@link ParamValueDAO#getParamAddress(int, int)}
     */
    @Deprecated
    public List<String> addressValues(int paramId) {
        log.warndMethod("addressValues", "getValue");

        return addressValues(paramId, null);
    }

    /**
     * Use {@link #getValue(int)}
     */
    @Deprecated
    public Set<String> listValueTitles(int paramId) {
        log.warndMethod("listValueTitles", "getValue");

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
        log.warndMethod("getParamText", "getValue");
        return paramDao.getParamText(objectId, paramId);
    }

    /**
     * Use {@link ParamValueDAO#getParamDate(int, int)}.
     */
    @Deprecated
    public Date getParamDate(int paramId) throws SQLException {
        log.warndMethod("getParamDate", "getValue");
        return paramDao.getParamDate(objectId, paramId);
    }

    /**
     * Use {@link ParamValueDAO#getParamDateTime(int, int)}.
     */
    @Deprecated
    public Date getParamDateTime(int paramId) throws SQLException {
        log.warndMethod("getParamDateTime", "getValue");
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
