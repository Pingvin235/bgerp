package ru.bgcrm.dao.expression;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.bgerp.util.Log;

import ru.bgcrm.cache.ParameterCache;
import ru.bgcrm.dao.ParamValueDAO;
import ru.bgcrm.model.FileData;
import ru.bgcrm.model.IdTitle;
import ru.bgcrm.model.param.Parameter;
import ru.bgcrm.model.param.ParameterAddressValue;
import ru.bgcrm.model.param.ParameterPhoneValue;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;

/**
 * Оболочка для {@link ParamValueDAO} используемого для конкретного объекта.
 */
public class ParamValueFunction {
    private static final Log log = Log.getLog();

    public static final String PARAM_FUNCTION_SUFFIX = "Param";

    private final ParamValueDAO paramDao;
    private final int objectId;
    private Map<Integer, Object> valuesCache = new HashMap<Integer, Object>();

    public ParamValueFunction(Connection con, int objectId) {
        this.paramDao = new ParamValueDAO(con);
        this.objectId = objectId;
    }

    /**
     * Возвращает коллекцию со значениями адресного параметра процесса.
     * @param paramId
     * @return
     */
    @SuppressWarnings("unchecked")
    private Collection<ParameterAddressValue> getParamAddressValues(int paramId, String formatName) {
        Collection<ParameterAddressValue> value = Collections.emptyList();

        try {
            Parameter param = ParameterCache.getParameter(paramId);
            if (param == null || !Parameter.TYPE_ADDRESS.equals(param.getType())) {
                log.error("Param not found: " + paramId + " or not address in expression");
                return Collections.emptySet();
            }

            value = (Collection<ParameterAddressValue>) valuesCache.get(paramId);
            if (value == null) {
                value = paramDao.getParamAddressExt(objectId, param.getId(), true, formatName).values();
                valuesCache.put(paramId, value);
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }

        return value;
    }

    /**
     * Возвращает список строк со значениями адресного параметра, форматирование по-умолчанию.
     * @param paramId
     * @return
     */
    public List<String> addressValues(int paramId) {
        return addressValues(paramId, null);
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

    /**
     * Возвращает набор с кодами значений спискового параметра процесса.
     * @param paramId
     * @return
     */
    @SuppressWarnings("unchecked")
    public Set<Integer> listValueIds(int paramId) {
        Set<Integer> result = Collections.emptySet();

        try {
            Parameter param = ParameterCache.getParameter(paramId);
            if (param == null || !Parameter.TYPE_LIST.equals(param.getType())) {
                log.error("Param not found: " + paramId + " or not list in expression");
                return Collections.emptySet();
            }

            result = (Set<Integer>) valuesCache.get(paramId);
            if (result == null) {
                result = paramDao.getParamList(objectId, paramId);
                valuesCache.put(paramId, result);
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }

        return result;
    }

    /**
     * Возвращает набор со строковыми значениями спискового параметра процесса.
     * @param paramId
     * @return
     */
    @SuppressWarnings("unchecked")
    public Set<String> listValueTitles(int paramId) {
        Set<String> result = Collections.emptySet();

        try {
            Parameter param = ParameterCache.getParameter(paramId);
            if (param == null || !Parameter.TYPE_LIST.equals(param.getType())) {
                log.error("Param not found: " + paramId + " or not list in expression");
                return Collections.emptySet();
            }

            result = (Set<String>) valuesCache.get(paramId);
            if (result == null) {
                result = new HashSet<String>();
                for (IdTitle value : paramDao.getParamListWithTitles(objectId, paramId)) {
                    result.add(value.getTitle());
                }
                valuesCache.put(paramId, result);
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }

        return result;
    }

    /**
     * Возвращает строковое представление параметра процесса.
     * Если параметр содержит несколько значений - они отображаются через запятую.
     * Поддержано для параметров text, date, datetime, phone, file.
     * @param paramId
     * @return
     */
    public String getValue(int paramId) {
        String result = "";

        try {
            Parameter param = ParameterCache.getParameter(paramId);
            switch (Parameter.Type.of(param.getType())) {
            case TEXT: {
                result = paramDao.getParamText(objectId, paramId);
                break;
            }
            case DATE: {
                result = TimeUtils.format(paramDao.getParamDate(objectId, paramId), param.getDateParamFormat());
                break;
            }
            case DATETIME: {
                result = TimeUtils.format(paramDao.getParamDateTime(objectId, paramId), param.getDateParamFormat());
                break;
            }
            case PHONE: {
                ParameterPhoneValue value = paramDao.getParamPhone(objectId, paramId);
                result = value != null ? value.getValue() : "";
                break;
            }
            case FILE: {
                SortedMap<Integer, FileData> value = paramDao.getParamFile(objectId, paramId);
                result = Utils.toString(value.keySet());
                break;
            }
            case ADDRESS:
                break;
            case BLOB:
                break;
            case EMAIL:
                break;
            case LIST:
                break;
            case LISTCOUNT:
                break;
            case TREE:
                break;
            default:
                break;
            }

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }

        return result;
    }

    /**
     * Возвращает значение текстового параметра процесса.
     * @param paramId
     * @return
     * @throws SQLException
     */
    public String getParamText(int paramId) throws SQLException {
        return paramDao.getParamText(objectId, paramId);
    }

    /**
     * Возвращает значение date параметра процесса.
     * @param paramId
     * @return
     * @throws SQLException
     */
    public Date getParamDate(int paramId) throws SQLException {
        return paramDao.getParamDate(objectId, paramId);
    }

    /**
     * Возвращает значение datetime параметра процесса.
     * @param paramId
     * @return
     * @throws SQLException
     */
    public Date getParamDateTime(int paramId) throws SQLException {
        return paramDao.getParamDateTime(objectId, paramId);
    }

    /**
     * Возвращает первое значение параметра типа phone без форматирования, т.е. только цифры.
     * @param paramId
     * @return
     * @throws SQLException
     */
    public String getParamPhoneNoFormat(int paramId) throws SQLException {
        ParameterPhoneValue value = paramDao.getParamPhone(objectId, paramId);
        if (!value.getItemList().isEmpty()) {
            return value.getItemList().get(0).getPhone();
        }
        return null;
    }
}
