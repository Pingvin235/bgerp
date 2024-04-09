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
import java.util.SortedMap;
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
 * Wrapper for {@link ParamValueDAO} with an object context.
 *
 * @author Shamil Vakhitov
 */
public class ParamValueFunction {
    private static final Log log = Log.getLog();

    public static final String PARAM_FUNCTION_SUFFIX = "Param";

    private final ParamValueDAO paramDao;
    private final int objectId;

    public ParamValueFunction(Connection con, int objectId) {
        this.paramDao = new ParamValueDAO(con);
        this.objectId = objectId;
    }

    /**
     * Возвращает список строк со значениями адресного параметра, формат указан.
     * @param paramId
     * @return
     */
    public List<String> addressValues(int paramId, String formatName) {
        List<String> result = new ArrayList<String>();

        Collection<ParameterAddressValue> value = getParamAddressValues(paramId, formatName);
        for (ParameterAddressValue addr : value) {
            result.add(addr.getValue());
        }

        return result;
    }

    /**
     * Возвращает набор с кодами городов адресного параметра процесса.
     * @param paramId
     * @return
     */
    public Set<Integer> addressCityIds(int paramId) {
        Collection<ParameterAddressValue> value = getParamAddressValues(paramId, null);

        Set<Integer> result = new HashSet<Integer>(value.size());
        for (ParameterAddressValue addr : value) {
            result.add(addr.getHouse().getAddressStreet().getCityId());
        }

        return result;
    }

    /**
     * Возвращает набор с кодами улиц адресного параметра процесса.
     * @param paramId
     * @return
     */
    public Set<Integer> addressStreetIds(int paramId) {
        Collection<ParameterAddressValue> value = getParamAddressValues(paramId, null);

        Set<Integer> result = new HashSet<Integer>(value.size());
        for (ParameterAddressValue addr : value) {
            result.add(addr.getHouse().getStreetId());
        }

        return result;
    }

    /**
     * Возвращает набор с кодами кварталов адресного параметра процесса.
     * @param paramId
     * @return
     */
    public Set<Integer> addressQuarterIds(int paramId) {
        Collection<ParameterAddressValue> value = getParamAddressValues(paramId, null);

        Set<Integer> result = new HashSet<Integer>(value.size());
        for (ParameterAddressValue addr : value) {
            result.add(addr.getHouse().getQuarterId());
        }

        return result;
    }

    /**
     * Возвращает набор с кодами районов из адресного параметра процесса.
     * @param paramId
     * @return
     */
    public Set<Integer> addressAreaIds(int paramId) {
        Collection<ParameterAddressValue> value = getParamAddressValues(paramId, null);

        Set<Integer> result = new HashSet<Integer>(value.size());
        for (ParameterAddressValue addr : value) {
            result.add(addr.getHouse().getAreaId());
        }

        return result;
    }

    private Collection<ParameterAddressValue> getParamAddressValues(int paramId, String formatName) {
        Collection<ParameterAddressValue> value = Collections.emptyList();

        try {
            Parameter param = ParameterCache.getParameter(paramId);
            if (param == null || !Parameter.TYPE_ADDRESS.equals(param.getType())) {
                log.error("Param not found {} or not address in expression", paramId);
                return Collections.emptySet();
            }

            value = paramDao.getParamAddressExt(objectId, param.getId(), true, formatName).values();
        } catch (SQLException e) {
            log.error(e);
        }

        return value;
    }

    /**
     * Selects parameter values for the current object.
     * @param paramId the parameter ID.
     * @return string representation of the parameter value.
     */
    public String getValue(int paramId) {
        try {
            Parameter param = ParameterCache.getParameter(paramId);
            switch (Parameter.Type.of(param.getType())) {
                case ADDRESS -> {
                    return paramDao.getParamAddress(objectId, paramId).values().stream()
                        .map(ParameterAddressValue::toString)
                        .collect(Collectors.joining("; "));
                }
                case BLOB -> {
                    return paramDao.getParamBlob(objectId, paramId);
                }
                case DATE -> {
                    return TimeUtils.format(paramDao.getParamDate(objectId, paramId), param.getDateParamFormat());
                }
                case DATETIME -> {
                    return TimeUtils.format(paramDao.getParamDateTime(objectId, paramId), param.getDateParamFormat());
                }
                case PHONE -> {
                    ParameterPhoneValue value = paramDao.getParamPhone(objectId, paramId);
                    return value != null ? value.toString() : "";
                }
                case TEXT -> {
                    return paramDao.getParamText(objectId, paramId);
                }
                case FILE -> {
                    SortedMap<Integer, FileData> value = paramDao.getParamFile(objectId, paramId);
                    return Utils.toString(value.keySet());
                }
                case EMAIL -> {
                    return "type 'email' is not supported yet";
                }
                case LIST -> {
                    return Utils.getObjectTitles(param.getListParamValues(), paramDao.getParamList(objectId, paramId));
                }
                case LISTCOUNT -> {
                    return "type 'listcount' is not supported yet";
                }
                case MONEY -> {
                    return Utils.format(paramDao.getParamMoney(objectId, paramId));
                }
                case TREE -> {
                    return "type 'tree' is not supported yet";
                }
                case TREECOUNT -> {
                    return "type 'treecount' is not supported yet";
                }
            }

        } catch (SQLException e) {
            log.error(e);
            return e.getMessage();
        }

        return "";
    }

    // deprecated methods begin

    /**
     * Use {@link ParamValueDAO#getParamAddress(int, int)}
     */
    @Deprecated
    public List<String> addressValues(int paramId) {
        log.warndMethod("addressValues", "ParamValueDAO.getParamAddress");

        return addressValues(paramId, null);
    }

    /**
     * Use {@link ParamValueDAO#getParamList(int, int)}
     */
    @Deprecated
    public Set<Integer> listValueIds(int paramId) {
        log.warndMethod("listValueIds", "paramValueDAO.getParamList");

        Set<Integer> result = Collections.emptySet();

        try {
            Parameter param = ParameterCache.getParameter(paramId);
            if (param == null || !Parameter.TYPE_LIST.equals(param.getType())) {
                log.error("Param not found: " + paramId + " or not list in expression");
                return Collections.emptySet();
            }

            result = paramDao.getParamList(objectId, paramId);
        } catch (SQLException e) {
            log.error(e);
        }

        return result;
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

            result = new HashSet<String>();
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
        log.warndMethod("getParamText", "ParamValueDAO.getParamText");
        return paramDao.getParamText(objectId, paramId);
    }

    /**
     * Use {@link ParamValueDAO#getParamDate(int, int)}.
     */
    @Deprecated
    public Date getParamDate(int paramId) throws SQLException {
        log.warndMethod("getParamDate", "ParamValueDAO.getParamDate");
        return paramDao.getParamDate(objectId, paramId);
    }

    /**
     * Use {@link ParamValueDAO#getParamDateTime(int, int)}.
     */
    @Deprecated
    public Date getParamDateTime(int paramId) throws SQLException {
        log.warndMethod("getParamDateTime", "ParamValueDAO.getParamDateTime");
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
