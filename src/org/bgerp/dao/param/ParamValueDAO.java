package org.bgerp.dao.param;

import static org.bgerp.model.param.Parameter.LIST_PARAM_USE_DIRECTORY_KEY;
import static ru.bgcrm.dao.AddressDAO.LOAD_LEVEL_COUNTRY;
import static ru.bgcrm.dao.Tables.TABLE_FILE_DATA;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.bgerp.app.exception.BGException;
import org.bgerp.app.exception.BGMessageException;
import org.bgerp.app.l10n.Localization;
import org.bgerp.cache.ParameterCache;
import org.bgerp.dao.FileDataDAO;
import org.bgerp.model.base.Id;
import org.bgerp.model.base.IdStringTitle;
import org.bgerp.model.base.IdTitle;
import org.bgerp.model.base.IdTitleComment;
import org.bgerp.model.file.FileData;
import org.bgerp.model.param.Parameter;
import org.bgerp.model.param.ParameterValue;
import org.bgerp.util.Log;
import org.bgerp.util.sql.PreparedQuery;

import ru.bgcrm.dao.AddressDAO;
import ru.bgcrm.dao.CommonDAO;
import ru.bgcrm.model.customer.Customer;
import ru.bgcrm.model.param.ParameterAddressValue;
import ru.bgcrm.model.param.ParameterEmailValue;
import ru.bgcrm.model.param.ParameterPhoneValue;
import ru.bgcrm.model.param.ParameterPhoneValueItem;
import ru.bgcrm.model.param.address.AddressHouse;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.user.User;
import ru.bgcrm.util.AddressUtils;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;

/**
 * Parameter values DAO. The primary required public methods are sorted alphabetically.
 * Dependency methods even public, called by those, are placed directly after the first usage.
 *
 * @author Shamil Vakhitov
 */
public class ParamValueDAO extends CommonDAO {
    private static final Log log = Log.getLog();

    public static final String[] TABLE_NAMES = {
        Tables.TABLE_PARAM_ADDRESS,
        Tables.TABLE_PARAM_BLOB,
        Tables.TABLE_PARAM_DATE,
        Tables.TABLE_PARAM_DATETIME,
        Tables.TABLE_PARAM_EMAIL,
        Tables.TABLE_PARAM_FILE,
        Tables.TABLE_PARAM_LIST,
        Tables.TABLE_PARAM_LISTCOUNT,
        Tables.TABLE_PARAM_MONEY,
        Tables.TABLE_PARAM_PHONE, Tables.TABLE_PARAM_PHONE_ITEM,
        Tables.TABLE_PARAM_TEXT,
        Tables.TABLE_PARAM_TREE
    };

    public static final String COPY_PARAMS_SEPARATORS = ";,";

    /** Write param changes history. */
    private boolean history;
    /** User ID for changes history. */
    private int userId;

    public ParamValueDAO(Connection con) {
        super(con);
    }

    public ParamValueDAO(Connection con, boolean history, int userId) {
        this(con);
        this.history = history;
        this.userId = userId;
    }

    /**
     * Копирует параметр с объекта на объект.
     * @param fromObjectId object ID исходного.
     * @param toObjectId object ID целевого.
     * @param paramId коды параметра.
     */
    public void copyParam(int fromObjectId, int toObjectId, int paramId) throws SQLException, BGException {
        copyParam(fromObjectId, paramId, toObjectId, paramId);
    }

    /**
     * Копирует параметр с объекта на объект. Параметры должны быть одного типа.
     * @param fromObjectId object ID исходного.
     * @param fromParamId param ID исходного.
     * @param toObjectId object ID целевого
     * @param toParamId param ID целевого.
     */
    public void copyParam(int fromObjectId, int fromParamId, int toObjectId, int toParamId) throws SQLException, BGException {
        String query = null;
        ArrayList<PreparedStatement> psList = new ArrayList<>();

        Parameter paramFrom = ParameterCache.getParameter(fromParamId);
        if (paramFrom == null) {
            throw new BGException("Param not found: " + fromParamId);
        }

        Parameter paramTo = ParameterCache.getParameter(toParamId);
        if (paramTo == null) {
            throw new BGException("Param not found: " + toParamId);
        }

        if (!paramFrom.getType().equals(paramTo.getType())) {
            throw new BGException("Different copy param types.");
        }

        final var paramType = Parameter.Type.of(paramFrom.getType());

        switch (paramType) {
            case ADDRESS -> {
                query = SQL_INSERT_IGNORE + Tables.TABLE_PARAM_ADDRESS
                        + " (id, param_id, n, house_id, flat, room, pod, floor, value, comment) "
                        + "SELECT ?, ?, n, house_id, flat, room, pod, floor, value, comment " + SQL_FROM
                        + Tables.TABLE_PARAM_ADDRESS + " WHERE id=? AND param_id=?";
                psList.add(con.prepareStatement(query));
            }
            case EMAIL -> {
                query = "INSERT INTO " + Tables.TABLE_PARAM_EMAIL + " (id, param_id, n, value) " + "SELECT ?, ?, n, value "
                        + "FROM " + Tables.TABLE_PARAM_EMAIL + " WHERE id=? AND param_id=?";
                psList.add(con.prepareStatement(query));
            }
            case LIST -> {
                query = SQL_INSERT_IGNORE + Tables.TABLE_PARAM_LIST + "(id, param_id, value, comment)"
                        + SQL_SELECT + "?, ?, value, comment "
                        + SQL_FROM + Tables.TABLE_PARAM_LIST
                        + SQL_WHERE + "id=? AND param_id=?";
                psList.add(con.prepareStatement(query));
            }
            case LISTCOUNT -> {
                query = SQL_INSERT_INTO + Tables.TABLE_PARAM_LISTCOUNT + "(id, param_id, value, count)"
                        + SQL_SELECT + "?, ?, value, count"
                        + SQL_FROM + Tables.TABLE_PARAM_LISTCOUNT + SQL_WHERE + "id=? AND param_id=?";
                psList.add(con.prepareStatement(query));
            }
            case TREE -> {
                query = "INSERT INTO " + Tables.TABLE_PARAM_TREE + "(id, param_id, value) SELECT ?, ?, value " + "FROM "
                        + Tables.TABLE_PARAM_TREE + " WHERE id=? AND param_id=?";
                psList.add(con.prepareStatement(query));
            }
            case TREECOUNT -> {
                query = SQL_INSERT_INTO + Tables.TABLE_PARAM_TREECOUNT + "(id, param_id, value, count)" + SQL_SELECT + "?, ?, value, count" + SQL_FROM
                        + Tables.TABLE_PARAM_TREECOUNT + SQL_WHERE + "id=? AND param_id=?";
                psList.add(con.prepareStatement(query));
            }
            case DATE, DATETIME, MONEY, TEXT, BLOB, PHONE -> {
                String tableName = "param_" + paramType.toString().toLowerCase();

                query = "INSERT INTO " + tableName + " (id, param_id, value) " + "SELECT ?, ?, value " + "FROM "
                        + tableName + " WHERE id=? AND param_id=?";
                psList.add(con.prepareStatement(query));

                if (Parameter.Type.PHONE == paramType) {
                    query = "INSERT INTO " + Tables.TABLE_PARAM_PHONE_ITEM
                            + " (id, param_id, n, phone, comment) "
                            + "SELECT ?, ?, n, phone, comment" + SQL_FROM + Tables.TABLE_PARAM_PHONE_ITEM
                            + " WHERE id=? AND param_id=?";
                    psList.add(con.prepareStatement(query));
                }
            }
            case FILE -> {
                query = "INSERT INTO " + Tables.TABLE_PARAM_FILE + "(id, param_id, n, value) "
                        + "SELECT ?, ?, n, value FROM " + Tables.TABLE_PARAM_FILE
                        + " WHERE id=? AND param_id=?";
                psList.add(con.prepareStatement(query));
            }
        }

        for (PreparedStatement ps : psList) {
            ps.setInt(1, toObjectId);
            ps.setInt(2, toParamId);
            ps.setInt(3, fromObjectId);
            ps.setInt(4, fromParamId);
            ps.executeUpdate();

            ps.close();
        }
    }

    /**
     * Копирует параметры с объекта на другой объект по указанной конфигурации.
     * @param fromObjectId исходный объект.
     * @param toObjectId целевой объект.
     * @param copyMapping конфигурация.
     * @throws SQLException, BGException
     */
    public void copyParams(int fromObjectId, int toObjectId, String copyMapping) throws SQLException, BGException {
        if (Utils.isBlankString(copyMapping)) {
            return;
        }

        for (String token : Utils.toList(copyMapping, COPY_PARAMS_SEPARATORS)) {
            String[] pair = token.split(":");
            if (pair.length == 2) {
                copyParam(fromObjectId, Utils.parseInt(pair[0]), toObjectId, Utils.parseInt(pair[1]));
            } else if (Utils.parseInt(token) > 0) {
                int paramId = Utils.parseInt(token);
                copyParam(fromObjectId, paramId, toObjectId, paramId);
            } else
                log.error("Incorrect copy param mapping: {}", token);
        }
    }

    /**
     * Копирует параметры с объекта на объект
     * @param fromObjectId object ID исходного.
     * @param toObjectId object ID целевого.
     * @param paramIds коды параметров.
     * @throws SQLException, BGException
     */
    public void copyParams(int fromObjectId, int toObjectId, Collection<Integer> paramIds) throws SQLException, BGException {
        for (int paramId : paramIds) {
            copyParam(fromObjectId, paramId, toObjectId, paramId);
        }
    }

    /**
     * Удаляет параметры объекта.
     * @param objectType тип объекта.
     * @param id object ID
     * @throws SQLException
     */
    public void deleteParams(String objectType, int id) throws SQLException {
        String query = SQL_DELETE + "pl" + SQL_FROM + Tables.TABLE_PARAM_LOG + "AS pl"
            + SQL_INNER_JOIN + Tables.TABLE_PARAM_PREF + "AS pref ON pl.param_id=pref.id AND pref.object='" + objectType + "'"
            + SQL_WHERE + "pl.object_id=?";
        try (var pq = new PreparedQuery(con, query)) {
            pq.addInt(id);
            pq.executeUpdate();
        }

        for (String tableName : TABLE_NAMES) {
            query =  SQL_DELETE + "pv" + SQL_FROM + tableName + " AS pv"
                + SQL_INNER_JOIN + Tables.TABLE_PARAM_PREF + "AS pref ON pv.param_id=pref.id AND pref.object=?"
                + SQL_WHERE + "pv.id=?";

            try (var ps = con.prepareStatement(query)) {
                ps.setString(1, objectType);
                ps.setInt(2, id);
                ps.executeUpdate();
            }
        }
    }

    /**
     * Возвращает адресный параметр объекта.
     * @param id - код объекта.
     * @param paramId - param ID.
     * @param position - позиция, начиная от 1, если в параметре установлены несколько значений.
     * @return
     * @throws SQLException
     */
    public ParameterAddressValue getParamAddress(int id, int paramId, int position) throws SQLException {
        ParameterAddressValue result = null;

        String query = "SELECT * FROM " + Tables.TABLE_PARAM_ADDRESS + "WHERE id=? AND param_id=? AND n=? " + "LIMIT 1";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, id);
        ps.setInt(2, paramId);
        ps.setInt(3, position);

        ResultSet rs = ps.executeQuery();
        if (rs.next())
            result = getParameterAddressValueFromRs(rs, "");
        ps.close();

        return result;
    }

    /**
     * Возвращает значения адресного параметра объекта.
     * @param id - код объекта.
     * @param paramId - param ID.
     * @return ключ - позиция, значение - значение на позиции.
     * @throws SQLException
     */
    public SortedMap<Integer, ParameterAddressValue> getParamAddress(int id, int paramId) throws SQLException {
        return getParamAddress(id, paramId, false, null);
    }

    /**
     * Возвращает значения адресного параметра объекта.
     * @param id - код объекта.
     * @param paramId - param ID.
     * @param loadDirs - признак необходимости загрузить справочники, чтобы был корректно заполнен {@link ParameterAddressValue#getHouse()}/
     * @return ключ - позиция, значение - значение на позиции.
     * @throws SQLException
     */
    public SortedMap<Integer, ParameterAddressValue> getParamAddress(int id, int paramId, boolean loadDirs) throws SQLException {
        return getParamAddress(id, paramId, loadDirs, null);
    }

    /**
     * Возвращает значения адресного параметра объекта.
     * @param id - код объекта.
     * @param paramId - param ID.
     * @param loadDirs - признак необходимости загрузить справочники, чтобы был корректно заполнен {@link ParameterAddressValue#getHouse()}.
     * @param formatName - наименование формата адреса из конфигурации, с помощью которого форматировать значение адреса.
     * @return ключ - позиция, значение - значение на позиции.
     * @throws SQLException
     */
    public SortedMap<Integer, ParameterAddressValue> getParamAddress(int id, int paramId, boolean loadDirs, String formatName)
            throws SQLException {
        SortedMap<Integer, ParameterAddressValue> result = new TreeMap<>();

        StringBuilder query = new StringBuilder(300);
        query.append("SELECT * FROM " + Tables.TABLE_PARAM_ADDRESS + " AS param ");
        if (loadDirs) {
            query.append(" LEFT JOIN " + Tables.TABLE_ADDRESS_HOUSE + " AS house ON param.house_id=house.id ");
            AddressDAO.addHouseSelectQueryJoins(query, LOAD_LEVEL_COUNTRY);
        }
        query.append(" WHERE param.id=? AND param.param_id=?" + SQL_ORDER_BY + "param.n");

        PreparedStatement ps = con.prepareStatement(query.toString());
        ps.setInt(1, id);
        ps.setInt(2, paramId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            result.put(rs.getInt("n"), getParameterAddressValueFromRs(rs, "param.", loadDirs, formatName));
        }
        ps.close();

        return result;
    }

    private ParameterAddressValue getParameterAddressValueFromRs(ResultSet rs, String prefix) throws SQLException {
        return getParameterAddressValueFromRs(rs, "", false, null);
    }

    private ParameterAddressValue getParameterAddressValueFromRs(ResultSet rs, String prefix, boolean loadDirs, String formatName)
            throws SQLException {
        ParameterAddressValue result = new ParameterAddressValue();

        result.setHouseId(rs.getInt(prefix + "house_id"));
        result.setFlat(rs.getString(prefix + "flat"));
        result.setRoom(rs.getString(prefix + "room"));
        result.setPod(rs.getInt(prefix + "pod"));
        result.setFloor((Integer) rs.getObject(prefix + "floor"));
        result.setValue(rs.getString(prefix + "value"));
        result.setComment(rs.getString(prefix + "comment"));

        if (loadDirs) {
            result.setHouse(AddressDAO.getAddressHouseFromRs(rs, "house.", LOAD_LEVEL_COUNTRY));
            if (Utils.notBlankString(formatName)) {
                result.setValue(AddressUtils.buildAddressValue(result, null, formatName));
            }
        }

        return result;
    }

    /**
     * Selects a value for parameter type 'blob'.
     * @param id object ID
     * @param paramId param ID
     * @return
     * @throws SQLException
     */
    public String getParamBlob(int id, int paramId) throws SQLException {
        return getTextParam(id, paramId, Tables.TABLE_PARAM_BLOB);
    }

    private String getTextParam(int id, int paramId, String table) throws SQLException {
        String result = null;

        StringBuilder query = new StringBuilder();

        query.append(SQL_SELECT);
        query.append("value");
        query.append(SQL_FROM);
        query.append(table);
        query.append(SQL_WHERE);
        query.append("id=? AND param_id=? LIMIT 1");

        PreparedStatement ps = con.prepareStatement(query.toString());
        ps.setInt(1, id);
        ps.setInt(2, paramId);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            result = rs.getString(1);
        }
        ps.close();

        return result;
    }

    /**
     * Selects a value for parameter type 'date'.
     * @param id object ID
     * @param paramId param ID
     * @return
     * @throws SQLException
     */
    public Date getParamDate(int id, int paramId) throws SQLException {
        return getParamDate(id, paramId, Tables.TABLE_PARAM_DATE);
    }

    /**
     * Selects a value for parameter type 'datetime'.
     * @param id object ID
     * @param paramId param ID
     * @return
     * @throws SQLException
     */
    public Date getParamDateTime(int id, int paramId) throws SQLException {
        return getParamDate(id, paramId, Tables.TABLE_PARAM_DATETIME);
    }

    private Date getParamDate(int id, int paramId, String table) throws SQLException {
        Date result = null;

        String query = "SELECT value FROM " + table + " WHERE id=? AND param_id=? LIMIT 1";

        PreparedStatement ps = con.prepareStatement(query.toString());
        ps.setInt(1, id);
        ps.setInt(2, paramId);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            result = rs.getTimestamp(1);
        }
        ps.close();

        return result;
    }

    /**
     * Selects values for parameter type 'email'.
     * @param id object ID
     * @param paramId param ID
     * @return key - param value position, value - a value itself.
     * @throws SQLException
     */
    public SortedMap<Integer, ParameterEmailValue> getParamEmail(int id, int paramId) throws SQLException {
        SortedMap<Integer, ParameterEmailValue> emailItems = new TreeMap<>();

        String query = "SELECT * FROM " + Tables.TABLE_PARAM_EMAIL + "WHERE id=? AND param_id=? " + "ORDER BY n ";

        PreparedStatement ps = con.prepareStatement(query.toString());
        ps.setInt(1, id);
        ps.setInt(2, paramId);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            emailItems.put(rs.getInt("n"), new ParameterEmailValue(rs.getString("value"), rs.getString("comment")));
        }
        ps.close();

        return emailItems;
    }

    /**
     * Selects a value for parameter type 'file'.
     * @param id object ID
     * @param paramId param ID
     * @param position position number for multiple values.
     * @return
     * @throws SQLException
     */
    public FileData getParamFile(int id, int paramId, int position) throws SQLException {
        FileData result = null;

        String query = "SELECT fd.*, pf.n FROM " + Tables.TABLE_PARAM_FILE + " AS pf "
            + "INNER JOIN " + TABLE_FILE_DATA + " AS fd ON pf.value=fd.id "
            + "WHERE pf.id=? AND pf.param_id=? AND pf.n=? LIMIT 1";

        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, id);
        ps.setInt(2, paramId);
        ps.setInt(3, position);

        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            result = FileDataDAO.getFromRs(rs, "fd.");
        }
        ps.close();

        return result;
    }

    /**
     * Selects values for parameter type 'file'.
     * @param id object ID
     * @param paramId param ID
     * @return map with key equals value's position.
     * @throws SQLException
     */
    public SortedMap<Integer, FileData> getParamFile(int id, int paramId) throws SQLException {
        SortedMap<Integer, FileData> fileMap = new TreeMap<>();

        String query = "SELECT fd.*, pf.n FROM " + Tables.TABLE_PARAM_FILE
            + " AS pf INNER JOIN " + TABLE_FILE_DATA + " AS fd ON pf.value=fd.id "
            + "WHERE pf.id=? AND pf.param_id=? ";

        var ps = con.prepareStatement(query);
        ps.setInt(1, id);
        ps.setInt(2, paramId);

        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            fileMap.put(rs.getInt("pf.n"), FileDataDAO.getFromRs(rs, "fd."));
        }
        ps.close();

        return fileMap;
    }

    /**
     * Selects a parameter value with type 'list'.
     * @param id object ID
     * @param paramId
     * @return Set с кодами значений.
     * @throws SQLException
     */
    public Set<Integer> getParamList(int id, int paramId) throws SQLException {
        Set<Integer> result = new HashSet<>();

        String query = "SELECT value FROM " + Tables.TABLE_PARAM_LIST + "WHERE id=? AND param_id=?";

        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, id);
        ps.setInt(2, paramId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            result.add(rs.getInt(1));
        }
        ps.close();

        return result;
    }

    /**
     * Selects a parameter value with type 'list' с комментариями значений.
     * @param id object ID
     * @param paramId param ID
     * @return ключ - код значения, значение - комментарий.
     * @throws SQLException
     */
    public Map<Integer, String> getParamListWithComments(int id, int paramId) throws SQLException {
        Map<Integer, String> result = new LinkedHashMap<>();

        String query = "SELECT value, comment FROM " + Tables.TABLE_PARAM_LIST + "WHERE id=? AND param_id=?";

        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, id);
        ps.setInt(2, paramId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            result.put(rs.getInt(1), rs.getString(2));
        }
        ps.close();

        return result;
    }

    /**
     * Selects a parameter value with type 'listcount'.
     * @param id object ID
     * @param paramId param ID
     * @return a map with key equals value IDs and values counts.
     * @throws SQLException
     */
    public Map<Integer, BigDecimal> getParamListCount(int id, int paramId) throws SQLException {
        Map<Integer, BigDecimal> result = new HashMap<>();

        String query = SQL_SELECT + "value, count" + SQL_FROM + Tables.TABLE_PARAM_LISTCOUNT + SQL_WHERE + "id=? AND param_id=?";

        try (var ps = con.prepareStatement(query)) {
            ps.setInt(1, id);
            ps.setInt(2, paramId);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                result.put(rs.getInt(1), rs.getBigDecimal(2));
        }

        return result;
    }

    /**
     * Selects a parameter value with type 'money'.
     * @param id object ID
     * @param paramId param ID
     * @return the value or {@code null}.
     * @throws SQLException
     */
    public BigDecimal getParamMoney(int id, int paramId) throws SQLException {
        return Utils.parseBigDecimal(getTextParam(id, paramId, Tables.TABLE_PARAM_MONEY), null);
    }

    /**
     * Selects a parameter value with type 'phone'.
     * @param id object ID
     * @param paramId param ID
     * @return the value or {@code null}.
     * @throws SQLException
     */
    public ParameterPhoneValue getParamPhone(int id, int paramId) throws SQLException {
        ParameterPhoneValue result = new ParameterPhoneValue();

        List<ParameterPhoneValueItem> itemList = new ArrayList<>();

        String query = SQL_SELECT + "phone, comment" + SQL_FROM + Tables.TABLE_PARAM_PHONE_ITEM
                + SQL_WHERE + "id=? AND param_id=?"
                + SQL_ORDER_BY + "n";
        try (var ps = con.prepareStatement(query.toString())) {
            ps.setInt(1, id);
            ps.setInt(2, paramId);
            var rs = ps.executeQuery();
            while (rs.next()) {
                itemList.add(getParamPhoneValueItemFromRs(rs));
            }
            result.setItemList(itemList);
        }

        return result;
    }

    public static ParameterPhoneValueItem getParamPhoneValueItemFromRs(ResultSet rs) throws SQLException {
        ParameterPhoneValueItem item = new ParameterPhoneValueItem();
        item.setPhone(rs.getString("phone"));
        item.setComment(rs.getString("comment"));
        return item;
    }

    /**
     * Selects a value of parameter type 'text'.
     * @param id object ID
     * @param paramId param ID
     * @return
     * @throws SQLException
     */
    public String getParamText(int id, int paramId) throws SQLException {
        return getTextParam(id, paramId, Tables.TABLE_PARAM_TEXT);
    }

    /**
     * Selects a parameter value with type 'tree'.
     * @param id object ID
     * @param paramId param ID
     * @return набор значений.
     * @throws SQLException
     */
    public Set<String> getParamTree(int id, int paramId) throws SQLException {
        Set<String> result = new HashSet<>();

        String query = "SELECT value FROM " + Tables.TABLE_PARAM_TREE + "WHERE id=? AND param_id=?";

        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, id);
        ps.setInt(2, paramId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            result.add(rs.getString(1));
        }
        ps.close();

        return result;
    }

    /**
     * Selects parameter values with type 'treecount'.
     * @param id object ID
     * @param paramId param ID
     * @return map with a key equal to the parameter value ID, and the value - value amount (count).
     * @throws SQLException
     */
    public Map<String, BigDecimal> getParamTreeCount(int id, int paramId) throws SQLException {
        Map<String, BigDecimal> result = new HashMap<>(10);

        String query = SQL_SELECT + "value, count" + SQL_FROM + Tables.TABLE_PARAM_TREECOUNT + SQL_WHERE + "id=? AND param_id=?";
        try (var ps = con.prepareStatement(query)) {
            ps.setInt(1, id);
            ps.setInt(2, paramId);

            var rs = ps.executeQuery();
            while (rs.next())
                result.put(rs.getString("value"), rs.getBigDecimal("count"));
        }

        return result;
    }

    /**
     * Проверяет заполненость параметра для объекта с кодом id.
     * @param id object ID
     * @param param параметр.
     * @return
     * @throws Exception
     */
    public boolean isParameterFilled(int id, Parameter param) throws Exception {
        String query = "SELECT * FROM param_" + param.getType() + " WHERE id=? AND param_id=? LIMIT 1";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, id);
        ps.setInt(2, param.getId());

        boolean result = ps.executeQuery().next();

        ps.close();

        return result;
    }

    /**
     * Переносит параметры при с кода объекта на -код объекта.
     * Используется при преобразовании не созданного до конца процесса с отрицательным кодом в созданный.
     * @param objectType
     * @param currentObjectId
     * @throws SQLException
     */
    public void objectIdInvert(String objectType, int currentObjectId) throws SQLException {
        // TODO: Invert records in TABLE_PARAM_LOG

        for (String tableName : TABLE_NAMES) {
            StringBuilder query = new StringBuilder();
            query.append("UPDATE ");
            query.append(tableName);
            query.append(" AS param");
            query.append(SQL_INNER_JOIN);
            query.append(Tables.TABLE_PARAM_PREF);
            query.append("AS pref ON param.param_id=pref.id AND pref.object=? ");
            query.append("SET param.id=?");
            query.append(SQL_WHERE);
            query.append("param.id=?");

            PreparedStatement ps = con.prepareStatement(query.toString());
            ps.setString(1, objectType);
            ps.setInt(2, -currentObjectId);
            ps.setInt(3, currentObjectId);
            ps.executeUpdate();
            ps.close();
        }
    }

    /**
     * Loads parameters for {@link ru.bgcrm.model.customer.Customer}, {@link ru.bgcrm.model.process.Process},
     * {@link ru.bgcrm.model.user.User} or {@link ru.bgcrm.model.param.address.AddressHouse}.
     * @param object customer or process.
     * @return
     * @throws SQLException
     */
    public Map<Integer, ParameterValue> parameters(Id object) throws SQLException {
        List<Parameter> parameters = null;
        if (object instanceof Customer) {
            parameters = ParameterCache.getObjectTypeParameterList(Customer.OBJECT_TYPE, ((Customer) object).getParamGroupId());
        } else if (object instanceof Process) {
            var type = ((Process) object).getType();
            parameters = Utils.getObjectList(ParameterCache.getParameterMap(), type.getProperties().getParameterIds());
        } else if (object instanceof User) {
            parameters = ParameterCache.getObjectTypeParameterList(User.OBJECT_TYPE);
        } else if (object instanceof AddressHouse) {
            parameters = ParameterCache.getObjectTypeParameterList(AddressHouse.OBJECT_TYPE);
        } else {
            throw new IllegalArgumentException("Unsupported object type: " + object);
        }

        return loadParameters(parameters, object.getId(), false)
            .stream()
            .collect(Collectors.toMap(pv -> pv.getParameter().getId(), pv -> pv));
    }

    /**
     * Updates, appends and deletes an address parameter value.
     * @param id - entity ID.
     * @param paramId - param ID.
     * @param position - starting from 1 value's position, 0 - appends a value with position MAX+1.
     * @param value - the value, {@code null} - delete value from the position if {@code position} > 0, else delete all the values.
     * @throws SQLException
     */
    public void updateParamAddress(int id, int paramId, int position, ParameterAddressValue value) throws SQLException {
        int index = 1;

        if (value == null) {
            PreparedQuery pq = new PreparedQuery(con);

            pq.addQuery(SQL_DELETE_FROM + Tables.TABLE_PARAM_ADDRESS + SQL_WHERE + "id=? AND param_id=? ");
            pq.addInt(id);
            pq.addInt(paramId);

            if (position > 0) {
                pq.addQuery(" AND n=?");
                pq.addInt(position);
            }

            pq.executeUpdate();
            pq.close();
        } else {
            if (value.getValue() == null)
                value.setValue(AddressUtils.buildAddressValue(value, con));

            try {
                if (position <= 0) {
                    position = 1;

                    String query = SQL_SELECT + "MAX(n) + 1" + SQL_FROM + Tables.TABLE_PARAM_ADDRESS + SQL_WHERE + "id=? AND param_id=?";
                    try (PreparedStatement ps = con.prepareStatement(query)) {
                        ps.setInt(1, id);
                        ps.setInt(2, paramId);

                        ResultSet rs = ps.executeQuery();
                        if (rs.next() && rs.getObject(1) != null) {
                            position = rs.getInt(1);
                        }
                    }

                    insertParamAddress(id, paramId, position, value);
                } else {
                    String query = SQL_UPDATE + Tables.TABLE_PARAM_ADDRESS
                            + SQL_SET + "value=?, house_id=?, flat=?, room=?, pod=?, floor=?, comment=?"
                            + SQL_WHERE + "id=? AND param_id=? AND n=?";
                    PreparedStatement ps = con.prepareStatement(query);

                    ps.setString(index++, value.getValue());
                    ps.setInt(index++, value.getHouseId());
                    ps.setString(index++, value.getFlat());
                    ps.setString(index++, value.getRoom());
                    ps.setInt(index++, value.getPod());
                    ps.setObject(index++, value.getFloor());
                    ps.setString(index++, value.getComment());
                    ps.setInt(index++, id);
                    ps.setInt(index++, paramId);
                    ps.setInt(index++, position);

                    int cnt = ps.executeUpdate();

                    ps.close();

                    if (cnt == 0)
                        insertParamAddress(id, paramId, position, value);
                }
            } catch (SQLIntegrityConstraintViolationException e) {
                log.debug("Duplicated address value failed to be inserted: {}", value);
            }
        }

        if (history) {
            StringBuffer result = new StringBuffer();
            SortedMap<Integer, ParameterAddressValue> addresses = getParamAddress(id, paramId);
            Iterator<Integer> it = addresses.keySet().iterator();
            while (it.hasNext()) {
                if (result.length() > 0) {
                    result.append("; ");
                }
                Integer key = it.next();
                result.append(addresses.get(key).getValue());
            }
            logParam(id, paramId, userId, result.toString());
        }
    }

    private void insertParamAddress(int id, int paramId, int position, ParameterAddressValue value) throws SQLException {
        int index = 1;

        String query = SQL_INSERT_INTO + Tables.TABLE_PARAM_ADDRESS
                + SQL_SET + "id=?, param_id=?, n=?, value=?, house_id=?, flat=?, room=?, pod=?, floor=?, comment=?";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(index++, id);
            ps.setInt(index++, paramId);
            ps.setInt(index++, position);
            ps.setString(index++, value.getValue());
            ps.setInt(index++, value.getHouseId());
            ps.setString(index++, value.getFlat());
            ps.setString(index++, value.getRoom());
            ps.setInt(index++, value.getPod());
            ps.setObject(index++, value.getFloor());
            ps.setString(index++, value.getComment());

            ps.executeUpdate();
        }
    }

    private void logParam(int id, int paramId, int userId, String newValue) throws SQLException {
        if (Utils.isBlankString(newValue)) {
            newValue = null;
        }

        ParamLogDAO paramLogDAO = new ParamLogDAO(this.con);

        if (newValue == null) {
            paramLogDAO.insertParamLog(id, paramId, userId, "");
        } else {
            paramLogDAO.insertParamLog(id, paramId, userId, newValue);
        }
    }

    /**
     * Обновляет строки адресных параметров для дома. Используется после изменений в адресных справочников,
     * для генерации корректных строк с адресными параметрами.
     * @param houseId код дома.
     * @throws SQLException
     */
    public void updateParamsAddressOnHouseUpdate(int houseId) throws SQLException {
        String query = "SELECT * FROM " + Tables.TABLE_PARAM_ADDRESS + " WHERE house_id=?";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, houseId);

        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            int id = rs.getInt("id");
            int paramId = rs.getInt("param_id");
            int pos = rs.getInt("n");

            ParameterAddressValue value = getParameterAddressValueFromRs(rs, "");
            value.setValue(AddressUtils.buildAddressValue(value, con));

            updateParamAddress(id, paramId, pos, value);
        }

        ps.close();
    }

    /**
     * Updates value for parameter type 'blob'
     * @param id object ID
     * @param paramId param ID
     * @param value значение, null или пустая строка - удалить значение.
     * @throws SQLException
     */
    public void updateParamBlob(int id, int paramId, String value) throws SQLException {
        if (Utils.isBlankString(value)) {
            value = null;
        }

        updateSimpleParam(id, paramId, value, Tables.TABLE_PARAM_BLOB);

        if (history) {
            logParam(id, paramId, userId, value != null ? Localization.getLocalizer().l("Length: {}", value.length()) : null);
        }
    }

    private void updateSimpleParam(int id, int paramId, Object value, String tableName) throws SQLException {
        if (value == null) {
            deleteFromParamTable(id, paramId, tableName);
        } else {
            StringBuilder query = new StringBuilder(200);

            query.append(SQL_UPDATE).append(tableName).append("SET value=?").append(SQL_WHERE).append("id=? AND param_id=?");

            PreparedStatement ps = con.prepareStatement(query.toString());
            ps.setObject(1, value);
            ps.setInt(2, id);
            ps.setInt(3, paramId);

            if (ps.executeUpdate() == 0) {
                ps.close();

                query.setLength(0);
                query.append(SQL_INSERT_INTO).append(tableName).append("(id, param_id, value)" + SQL_VALUES_3);

                ps = con.prepareStatement(query.toString());
                ps.setInt(1, id);
                ps.setInt(2, paramId);
                ps.setObject(3, value);
                ps.executeUpdate();
            }
            ps.close();
        }
    }

    /**
     * Updates value for parameter type 'date'
     * @param id object ID
     * @param paramId param ID
     * @param value the value, {@code null} - delete
     * @throws SQLException
     */
    public void updateParamDate(int id, int paramId, Date value) throws SQLException {
        updateSimpleParam(id, paramId, value, Tables.TABLE_PARAM_DATE);

        if (history) {
            logParam(id, paramId, userId, TimeUtils.format(value, TimeUtils.FORMAT_TYPE_YMD));
        }
    }

    /**
     * Updates value for parameter type 'datetime'
     * @param id object ID
     * @param paramId param ID
     * @param value the value, {@code null} - delete
     * @throws SQLException
     */
    public void updateParamDateTime(int id, int paramId, Date value) throws SQLException {
        updateSimpleParam(id, paramId, value, Tables.TABLE_PARAM_DATETIME);

        if (history) {
            logParam(id, paramId, userId, TimeUtils.format(value, TimeUtils.FORMAT_TYPE_YMDHMS));
        }
    }

    /**
     * Updates values for parameter type 'email'
     * @param id object ID
     * @param paramId param ID
     * @param values the values, {@code null} or empty - delete values in DB
     * @throws SQLException
    */
    public void updateParamEmail(int id, int paramId, List<ParameterEmailValue> values) throws SQLException {
        deleteFromParamTable(id, paramId, Tables.TABLE_PARAM_EMAIL);

        if (CollectionUtils.isNotEmpty(values)) {
            try (var ps = con.prepareStatement(SQL_INSERT_INTO + Tables.TABLE_PARAM_EMAIL + "(id, param_id, n, value, comment)" + SQL_VALUES_5)) {
                ps.setInt(1, id);
                ps.setInt(2, paramId);

                int n = 1;
                for (var value : values) {
                    ps.setInt(3, n++);
                    ps.setString(4, value.getValue());
                    ps.setString(5, value.getComment());
                    ps.executeUpdate();
                }
            }
        }

        if (history)
            logParam(id, paramId, userId, ParameterEmailValue.toString(values));
    }

    /**
     * Updates values for parameter type 'email'
     * @param id object ID
     * @param paramId param ID
     * @param position values' position, starting from 1, {@code 0} add a new value with position {@code MAX + 1}
     * @param value the value, {@code null} delete the values from the {@code position} more than 0, delete all values if {@code position} is 0
     * @throws SQLException
     */
    public void updateParamEmail(int id, int paramId, int position, ParameterEmailValue value) throws SQLException {
        int index = 1;

        if (value == null) {
            PreparedQuery psDelay = new PreparedQuery(con);

            psDelay.addQuery(SQL_DELETE_FROM + Tables.TABLE_PARAM_EMAIL + SQL_WHERE + "id=? AND param_id=?");
            psDelay.addInt(id);
            psDelay.addInt(paramId);

            if (position > 0) {
                psDelay.addQuery(" AND n=?");
                psDelay.addInt(position);
            }

            psDelay.executeUpdate();

            psDelay.close();
        } else {
            if (position <= 0) {
                position = 1;

                String query = "SELECT MAX(n) + 1 FROM " + Tables.TABLE_PARAM_EMAIL + " WHERE id=? AND param_id=?";
                PreparedStatement ps = con.prepareStatement(query);
                ps.setInt(1, id);
                ps.setInt(2, paramId);

                ResultSet rs = ps.executeQuery();
                if (rs.next() && rs.getObject(1) != null) {
                    position = rs.getInt(1);
                }
                ps.close();

                query = "INSERT INTO " + Tables.TABLE_PARAM_EMAIL + " SET id=?, param_id=?, n=?, value=?, comment=?";
                ps = con.prepareStatement(query);
                ps.setInt(index++, id);
                ps.setInt(index++, paramId);
                ps.setInt(index++, position);
                ps.setString(index++, value.getValue());
                ps.setString(index++, value.getComment());

                ps.executeUpdate();

                ps.close();
            } else {
                String query = "UPDATE " + Tables.TABLE_PARAM_EMAIL + " SET value=?, comment=?"
                        + " WHERE id=? AND param_id=? AND n=?";
                PreparedStatement ps = con.prepareStatement(query);

                ps.setString(index++, value.getValue());
                ps.setString(index++, value.getComment());
                ps.setInt(index++, id);
                ps.setInt(index++, paramId);
                ps.setInt(index++, position);

                ps.executeUpdate();

                ps.close();
            }
        }

        if (history) {
            logParam(id, paramId, userId, ParameterEmailValue.toString(getParamEmail(id, paramId).values()));
        }
    }

    /**
     * Updates values for parameter type 'file'
     * @param id object ID
     * @param paramId param ID
     * @param position position for multiple values, when is 0 - adding with new positions
     * @param fileData value for the given position, if {@code null} - removes a value from the position or all values with {@code position} == -1
     * @throws Exception
     */
    public void updateParamFile(int id, int paramId, int position, FileData fileData) throws Exception {
        // deletion
        if (fileData == null) {
            Map<Integer, FileData> currentValue = null;
            if (position == -1)
                currentValue = getParamFile(id, paramId);
            else {
                var value = getParamFile(id, paramId, position);
                currentValue = value != null ? Map.of(position, value) : Map.of();
            }

            if (!currentValue.isEmpty()) {
                for (var value : currentValue.values())
                    new FileDataDAO(con).delete(value);

                String query = SQL_DELETE_FROM + Tables.TABLE_PARAM_FILE + SQL_WHERE + "id=? AND param_id=?";
                try (var pq = new PreparedQuery(con, query)) {
                    pq.addInt(id);
                    pq.addInt(paramId);
                    if (position != -1) {
                        pq.addQuery(" AND n=?");
                        pq.addInt(position);
                    }
                    pq.executeUpdate();
                }
            }
        } else {
            if (fileData.getId() <= 0 && fileData.getData() != null) {
                try (var fos = new FileDataDAO(con).add(fileData)) {
                    fos.write(fileData.getData());
                }
            }

            if (position == 0) {
                var query = SQL_SELECT + "p.n, f.title" + SQL_FROM + Tables.TABLE_PARAM_FILE + "AS p" +
                    SQL_INNER_JOIN + ru.bgcrm.dao.Tables.TABLE_FILE_DATA + "AS f ON p.value=f.id" +
                    SQL_WHERE + "p.id=? AND p.param_id=?";
                try (var ps = con.prepareStatement(query)) {
                    ps.setInt(1, id);
                    ps.setInt(2, paramId);

                    int posMax = 0;

                    var rs = ps.executeQuery();
                    while (rs.next()) {
                        int pos = rs.getInt("n");
                        if (pos > posMax)
                            posMax = pos;

                        String title = rs.getString("title");
                        if (title.equals(fileData.getTitle()))
                            throw new BGMessageException("File '{}' was already uploaded", title);
                    }

                    position = posMax + 1;
                }
            }

            var query = SQL_INSERT_INTO + Tables.TABLE_PARAM_FILE + "(id, param_id, n, value)" + SQL_VALUES_4;
            try (var ps = con.prepareStatement(query)) {
                ps.setInt(1, id);
                ps.setInt(2, paramId);
                ps.setInt(3, position);
                ps.setInt(4, fileData.getId());
                ps.executeUpdate();
            }
        }

        if (history) {
            String values = "";

            String query = SQL_SELECT + "GROUP_CONCAT(fd.title SEPARATOR ', ')"
                + SQL_FROM + Tables.TABLE_PARAM_FILE + "AS pf "
                + SQL_INNER_JOIN + TABLE_FILE_DATA + "AS fd ON pf.value=fd.id"
                + SQL_WHERE + "pf.id=? AND pf.param_id=?";
            var pq = new PreparedQuery(con, query);
            pq.addInt(id).addInt(paramId);
            var rs = pq.executeQuery();

            if (rs.next())
                values = rs.getString(1);

            pq.close();

            logParam(id, paramId, userId, values);
        }
    }

    /**
     * Updates values without comments for parameter type 'list'
     * @param id object ID
     * @param paramId param ID
     * @param values the values
     * @throws SQLException
     */
    public void updateParamList(int id, int paramId, Set<Integer> values) throws SQLException {
        if (values == null)
            values = Set.of();

        updateParamListWithComments(id, paramId, values.stream().collect(Collectors.toMap(Function.identity(), unused -> "")));
    }

    /**
     * Updates values with comments for parameter type 'list'
     * @param id object ID
     * @param paramId param ID
     * @param values the values map, keys represent values, values - comments
     * @throws SQLException
     */
    public void updateParamListWithComments(int id, int paramId, Map<Integer, String> values) throws SQLException {
        deleteFromParamTable(id, paramId, Tables.TABLE_PARAM_LIST);

        try (var ps = con.prepareStatement(SQL_INSERT_INTO + Tables.TABLE_PARAM_LIST + "(id, param_id, value, comment)" + SQL_VALUES_4)) {
            ps.setInt(1, id);
            ps.setInt(2, paramId);
            for (Map.Entry<Integer, String> value : values.entrySet()) {
                ps.setInt(3, value.getKey());
                ps.setString(4, value.getValue());
                ps.executeUpdate();
            }
        }

        if (history) {
            var logRecord = new StringBuilder(30);
            for (var item : ParameterCache.getListParamValues(paramId)) {
                String comment = values.get(item.getId());
                if (comment == null)
                    continue;
                if (!logRecord.isEmpty())
                    logRecord.append(", ");
                logRecord.append(item.getTitle());
                if (Utils.notBlankString(comment)) {
                    logRecord
                        .append(" [")
                        .append(comment)
                        .append("]");
                }
            }
            logParam(id, paramId, userId, logRecord.toString());
        }
    }

    /**
     * Updates values for parameter type 'listcount'
     * @param id entity ID
     * @param paramId param ID
     * @param values map with key = value ID, and values with possible types: {@link String}, {@link BigDecimal}
     * @throws SQLException
     */
    public void updateParamListCount(int id, int paramId, Map<Integer, ?> values) throws SQLException {
        if (values == null)
            values = Map.of();

        deleteFromParamTable(id, paramId, Tables.TABLE_PARAM_LISTCOUNT);

        try (var ps = con.prepareStatement(SQL_INSERT_INTO + Tables.TABLE_PARAM_LISTCOUNT + "(id, param_id, value, count)" + SQL_VALUES_4)) {
            ps.setInt(1, id);
            ps.setInt(2, paramId);
            for (var me : values.entrySet()) {
                ps.setInt(3, me.getKey());
                Object value = me.getValue();

                BigDecimal count;

                if (value instanceof BigDecimal)
                    count = (BigDecimal) value;
                else if (value instanceof String)
                    count = Utils.parseBigDecimal((String) value);
                else
                    throw new IllegalArgumentException("Usupported value type: " + value);

                ps.setBigDecimal(4, count);
                ps.executeUpdate();
            }
        }

        if (history) {
            var logRecord = new StringBuilder(30);
            for (var item : ParameterCache.getListParamValues(paramId)) {
                var count = values.get(item.getId());
                if (count == null)
                    continue;
                Utils.addCommaSeparated(logRecord, item.getTitle() + ": " + count);
            }
            logParam(id, paramId, userId, logRecord.toString());
        }
    }

    /**
     * Updates value for parameter type 'money'
     * @param id object ID
     * @param paramId param ID
     * @param value the value, when {@code null} - delete
     * @throws SQLException
     */
    public void updateParamMoney(int id, int paramId, BigDecimal value) throws SQLException {
        updateSimpleParam(id, paramId, value, Tables.TABLE_PARAM_MONEY);

        if (history) {
            logParam(id, paramId, userId, String.valueOf(value));
        }
    }

    /**
     * Updates value for parameter type 'money'
     * @param id object ID
     * @param paramId parm ID.
     * @param value the value, when {@code null} or a blank string - delete
     * @throws SQLException
     */
    public void updateParamMoney(int id, int paramId, String value) throws SQLException {
        if (Utils.isBlankString(value))
            value = null;

        updateSimpleParam(id, paramId, Utils.parseBigDecimal(value), Tables.TABLE_PARAM_MONEY);

        if (history) {
            logParam(id, paramId, userId, value);
        }
    }

    /**
     * Updates values for parameter type 'phone'
     * @param id object ID
     * @param paramId param ID
     * @param value the values, {@code null} or empty {@code itemList} - delete values
     * @throws SQLException
     */
    public void updateParamPhone(int id, int paramId, ParameterPhoneValue value) throws SQLException {
        String newPhones = null;

        if (value == null || value.getItemList().size() == 0) {
            deleteFromParamTable(id, paramId, Tables.TABLE_PARAM_PHONE);
            deleteFromParamTable(id, paramId, Tables.TABLE_PARAM_PHONE_ITEM);
        } else {
            newPhones = value.toString();

            updateSimpleParam(id, paramId, newPhones, Tables.TABLE_PARAM_PHONE);

            deleteFromParamTable(id, paramId, Tables.TABLE_PARAM_PHONE_ITEM);

            int index = 1;

            String query = "INSERT INTO" + Tables.TABLE_PARAM_PHONE_ITEM
                    + "SET id=?, param_id=?, n=?, phone=?, comment=?";
            PreparedStatement ps = con.prepareStatement(query.toString());
            ps.setInt(index++, id);
            ps.setInt(index++, paramId);

            int n = 1;
            for (ParameterPhoneValueItem item : value.getItemList()) {
                index = 3;
                ps.setInt(index++, n++);
                ps.setString(index++, item.getPhone());
                ps.setString(index++, item.getComment());
                ps.executeUpdate();
            }
            ps.close();
        }

        if (history)
            logParam(id, paramId, userId, newPhones);
    }

    /**
     * Updates value for parameter type 'text'
     * @param id object ID
     * @param paramId param ID
     * @param value the value, {@code null} or emtpy string - delete value
     * @throws SQLException
     */
    public void updateParamText(int id, int paramId, String value) throws SQLException {
        if (Utils.isBlankString(value)) {
            value = null;
        }

        updateSimpleParam(id, paramId, value, Tables.TABLE_PARAM_TEXT);

        if (history) {
            logParam(id, paramId, userId, value);
        }
    }

    /**
     * Updates values for parameter type 'tree'
     * @param id object ID
     * @param paramId param ID
     * @param values the values, {@code null} or empty set - delete values
     * @throws SQLException
     */
    public void updateParamTree(int id, int paramId, Set<String> values) throws SQLException {
        if (values == null)
            values = Set.of();

        deleteFromParamTable(id, paramId, Tables.TABLE_PARAM_TREE);

        String query = SQL_INSERT_INTO + Tables.TABLE_PARAM_TREE + "(id, param_id, value)" + SQL_VALUES_3;

        PreparedStatement ps = con.prepareStatement(query.toString());
        ps.setInt(1, id);
        ps.setInt(2, paramId);
        for (String value : values) {
            ps.setString(3, value);
            ps.executeUpdate();
        }
        ps.close();

        if (history) {
            logParam(id, paramId, userId, Utils.getObjectTitles(getParamTreeWithTitles(id, paramId)));
        }
    }

    /**
     * Updates values for parameter type 'treecount'
     * @param id object ID
     * @param paramId param ID
     * @param values the values map (key - treecount value ID, value - 'count'), {@code null} or emtpy map - delete values
     * @throws SQLException
     */
    public void updateParamTreeCount(int id, int paramId, Map<String, BigDecimal> values) throws SQLException {
        if (values == null)
            values = Map.of();

        deleteFromParamTable(id, paramId, Tables.TABLE_PARAM_TREECOUNT);

        String query = SQL_INSERT_INTO + Tables.TABLE_PARAM_TREECOUNT + "(id, param_id, value, count)" + SQL_VALUES_4;

        try (var ps = con.prepareStatement(query)) {
            ps.setInt(1, id);
            ps.setInt(2, paramId);
            for (var me : values.entrySet()) {
                ps.setString(3, me.getKey());
                ps.setBigDecimal(4, me.getValue());
                ps.executeUpdate();
            }
        }

        if (history)
            logParam(id, paramId, userId, Parameter.Type.treeCountToString(paramId, values));
    }

    /**
     * Loads parameter's values.
     * @param paramList parameters list.
     * @param id entity id.
     * @param offEncryption decrypt pseudo encrypted values.
     * @throws SQLException
     */
    public List<ParameterValue> loadParameters(List<Parameter> paramList, int id, boolean offEncryption) throws SQLException {
        Map<String, List<Integer>> paramTypeMap = new HashMap<>();

        List<ParameterValue> result = new ArrayList<>(paramList.size());
        Map<Integer, ParameterValue> paramMap = new HashMap<>(paramList.size());

        for (Parameter parameter : paramList) {
            String type = parameter.getType();
            List<Integer> ids = paramTypeMap.get(type);
            if (ids == null) {
                paramTypeMap.put(type, ids = new ArrayList<>());
            }
            ids.add(parameter.getId());

            ParameterValue pvp = new ParameterValue(parameter);
            paramMap.put(parameter.getId(), pvp);

            result.add(pvp);
        }

        for (String type : paramTypeMap.keySet())
            updateParamValueMap(paramMap, type, paramTypeMap.get(type), id, offEncryption);

        return result;
    }

    /**
     * Loads parameters for a type.
     * @param paramMap target map.
     * @param type type.
     * @param ids IDs.
     * @param objectId object ID.
     * @throws SQLException
     */
    @SuppressWarnings("unchecked")
    private void updateParamValueMap(Map<Integer, ParameterValue> paramMap, String type, Collection<Integer> ids,
            int objectId, boolean offEncryption) throws SQLException {
        StringBuilder query = new StringBuilder(1000);

        if (Parameter.TYPE_ADDRESS.equals(type)) {
            query.append("SELECT param_id, n, value, house_id FROM param_");
            query.append(type);
            query.append(" WHERE id=? AND param_id IN ( ");
            query.append(Utils.toString(ids));
            query.append(" )" + SQL_ORDER_BY + "n");
        } else if (Parameter.TYPE_EMAIL.equals(type)) {
            query.append("SELECT param_id, n, value, comment FROM param_");
            query.append(type);
            query.append(" WHERE id=? AND param_id IN ( ");
            query.append(Utils.toString(ids));
            query.append(" )" + SQL_ORDER_BY + "n");
        } else if (Parameter.TYPE_FILE.equals(type)) {
            query.append("SELECT pf.param_id, pf.n, fd.* FROM " + Tables.TABLE_PARAM_FILE + " AS pf INNER JOIN " + TABLE_FILE_DATA
                    + " AS fd ON pf.value=fd.id " + " WHERE pf.id=? AND pf.param_id IN ( ");
            query.append(Utils.toString(ids));
            query.append(" )" + SQL_ORDER_BY + "n");
        } else if (Parameter.TYPE_LIST.equals(type)) {
            query.append(SQL_SELECT + "param_id, value, comment" + SQL_FROM + Tables.TABLE_PARAM_LIST + SQL_WHERE + "id=? AND param_id IN (")
                    .append(Utils.toString(ids)).append(")");
        } else if (Parameter.TYPE_LISTCOUNT.equals(type)) {
            query.append(SQL_SELECT + "param_id, value, count" + SQL_FROM + Tables.TABLE_PARAM_LISTCOUNT + SQL_WHERE + "id=? AND param_id IN (")
                    .append(Utils.toString(ids)).append(")");
        } else if (Parameter.TYPE_PHONE.equals(type)) {
            query.append("SELECT pi.param_id, pi.n, pi.phone, pi.comment " + SQL_FROM + Tables.TABLE_PARAM_PHONE_ITEM
                    + "AS pi WHERE pi.id=? AND pi.param_id IN ( ");
            query.append(Utils.toString(ids));
            query.append(" )" + SQL_ORDER_BY + "pi.n");
        } else if (Parameter.TYPE_TREE.equals(type)) {
            query.append(SQL_SELECT + "param_id, value" + SQL_FROM + Tables.TABLE_PARAM_TREE + SQL_WHERE + "id=? AND param_id IN (")
                    .append(Utils.toString(ids)).append(")");
        } else if (Parameter.TYPE_TREECOUNT.equals(type)) {
            query.append(SQL_SELECT + "param_id, value, count" + SQL_FROM + Tables.TABLE_PARAM_TREECOUNT + SQL_WHERE + "id=? AND param_id IN (")
                    .append(Utils.toString(ids)).append(")" + SQL_ORDER_BY + "value");
        } else {
            query.append("SELECT param_id, value FROM param_");
            query.append(type);
            query.append(" WHERE id=? AND param_id IN ( ");
            query.append(Utils.toString(ids));
            query.append(" )");
        }

        try (var ps = con.prepareStatement(query.toString())) {
            ps.setInt(1, objectId);
            var rs = ps.executeQuery();

            while (rs.next()) {
                final int paramId = rs.getInt(1);

                ParameterValue param = paramMap.get(paramId);

                if (Parameter.TYPE_ADDRESS.equals(type)) {
                    Map<Integer, ParameterAddressValue> values = (Map<Integer, ParameterAddressValue>) param.getValue();
                    if (values == null)
                        param.setValue(values = new TreeMap<>());

                    ParameterAddressValue val = new ParameterAddressValue();
                    val.setValue(rs.getString("value"));
                    val.setHouseId(rs.getInt("house_id"));

                    values.put(rs.getInt("n"), val);
                } else if (Parameter.TYPE_DATE.equals(type)) {
                    param.setValue(rs.getDate("value"));
                } else if (Parameter.TYPE_DATETIME.equals(type)) {
                    param.setValue(rs.getTimestamp("value"));
                } else if (Parameter.TYPE_EMAIL.equals(type)) {
                    Map<Integer, ParameterEmailValue> values = (Map<Integer, ParameterEmailValue>) param.getValue();
                    if (values == null)
                        param.setValue(values = new TreeMap<>());
                    values.put(rs.getInt("n"), new ParameterEmailValue(rs.getString("value"), rs.getString("comment")));
                } else if (Parameter.TYPE_FILE.equals(type)) {
                    Map<String, FileData> values = (Map<String, FileData>) param.getValue();
                    if (values == null)
                        param.setValue(values = new LinkedHashMap<>());

                    values.put(rs.getString("pf.n"), FileDataDAO.getFromRs(rs, "fd."));
                } else if (Parameter.TYPE_LIST.equals(type)) {
                    var values = (Map<Integer, String>) param.getValue();
                    if (values == null)
                        param.setValue(values = new TreeMap<>());
                    values.put(rs.getInt("value"), rs.getString("comment"));
                } else if (Parameter.TYPE_LISTCOUNT.equals(type)) {
                    var values = (Map<Integer, BigDecimal>) param.getValue();
                    if (values == null)
                        param.setValue(values = new TreeMap<>());
                    values.put(rs.getInt("value"), rs.getBigDecimal("count"));
                } else if (Parameter.TYPE_MONEY.equals(type)) {
                    param.setValue(rs.getBigDecimal("value"));
                } else if (Parameter.TYPE_PHONE.equals(type)) {
                    ParameterPhoneValue value = (ParameterPhoneValue)param.getValue();
                    if (value == null)
                        param.setValue(value = new ParameterPhoneValue());
                    value.addItem(getParamPhoneValueItemFromRs(rs));
                } else if (Parameter.TYPE_TREE.equals(type)) {
                    var values = (Set<String>) param.getValue();
                    if (values == null)
                        param.setValue(values = new TreeSet<>());
                    values.add(rs.getString("value"));
                } else if (Parameter.TYPE_TREECOUNT.equals(type)) {
                    var values = (Map<String, BigDecimal>) param.getValue();
                    if (values == null)
                        param.setValue(values = new TreeMap<String, BigDecimal>());
                    values.put(rs.getString("value"), rs.getBigDecimal("count"));
                } else {
                    if ("encrypted".equals(param.getParameter().getConfigMap().get("encrypt")) && !offEncryption) {
                        param.setValue("<ЗНАЧЕНИЕ ЗАШИФРОВАНО>");
                    } else {
                        param.setValue(rs.getString(2));
                    }
                }
            }
        }
    }

    private void addListTableJoin(StringBuilder query, String tableName) {
        query.append(SQL_LEFT_JOIN).append(tableName).append(" AS dir ON ");
        if (tableName.equals(Tables.TABLE_PARAM_LIST_VALUE))
            query.append(" val.param_id=dir.param_id AND val.value=dir.id ");
        else
            query.append(" val.value=dir.id ");
    }

    private void deleteFromParamTable(int id, int paramId, String tableName) throws SQLException {
        String query = SQL_DELETE_FROM + tableName + SQL_WHERE + "id=? AND param_id=?";
        try (var ps = con.prepareStatement(query)) {
            ps.setInt(1, id);
            ps.setInt(2, paramId);
            ps.executeUpdate();
        }
    }

    // DEPRECATED

    /**
     * Selects a value for parameter type 'email'.
     * @param id object ID
     * @param paramId param ID
     * @position param value position.
     * @return
     * @throws SQLException
     */
    @Deprecated
    public ParameterEmailValue getParamEmail(int id, int paramId, int position) throws SQLException {
        log.warndMethod("getParamEmail(int, int, int)", "getParamEmail(int, int)");

        ParameterEmailValue emailItem = null;

        String query = "SELECT * FROM " + Tables.TABLE_PARAM_EMAIL + "WHERE id=? AND param_id=? AND n=? " + "LIMIT 1";

        PreparedStatement ps = con.prepareStatement(query.toString());
        ps.setInt(1, id);
        ps.setInt(2, paramId);
        ps.setInt(3, position);

        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            emailItem = new ParameterEmailValue(rs.getString("value"), rs.getString("comment"));
        }
        ps.close();

        return emailItem;
    }

    /**
     * Selects a parameter value with type 'list' с наименованиями значений.
     * @param id object ID
     * @param paramId param ID
     * @return
     * @throws SQLException
     */
    @Deprecated
    public List<IdTitle> getParamListWithTitles(int id, int paramId) throws SQLException {
        log.warnd("getParamListWithTitles", "getParamList");

        List<IdTitleComment> values = getParamListWithTitlesAndComments(id, paramId);
        return new ArrayList<>(values);
    }

    /**
     * Selects a parameter value with type 'list' с наименованиями значений и примечаниями.
     * @param id object ID
     * @param paramId param ID
     * @return
     * @throws SQLException
     */
    @Deprecated
    public List<IdTitleComment> getParamListWithTitlesAndComments(int id, int paramId) throws SQLException {
        log.warnd("getParamListWithTitlesAndComments", "getParamList");

        List<IdTitleComment> result = new ArrayList<>();

        StringBuilder query = new StringBuilder();

        query.append(SQL_SELECT);
        query.append(" val.value, dir.title, val.comment ");
        query.append(SQL_FROM);
        query.append(Tables.TABLE_PARAM_LIST);
        query.append(" AS val ");
        addListTableJoin(query, ParameterCache.getParameter(paramId).getConfigMap().get(LIST_PARAM_USE_DIRECTORY_KEY, Tables.TABLE_PARAM_LIST_VALUE));
        query.append(SQL_WHERE);
        query.append(" val.id=? AND val.param_id=? ");

        PreparedStatement ps = con.prepareStatement(query.toString());
        ps.setInt(1, id);
        ps.setInt(2, paramId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            result.add(new IdTitleComment(rs.getInt(1), rs.getString(2), rs.getString(3)));
        }
        ps.close();

        return result;
    }

    /**
     * Selects a parameter value with type 'listcount' с наименованиями значений.
     * @param id object ID
     * @param paramId param ID
     * @return
     * @throws SQLException
     */
    @Deprecated
    public List<IdTitle> getParamListCountWithTitles(int id, int paramId) throws SQLException {
        log.warnd("getParamListCountWithTitles", "getParamListCount");

        List<IdTitle> result = new ArrayList<>();

        StringBuilder query = new StringBuilder();

        query.append(SQL_SELECT);
        query.append("val.value, dir.title");
        query.append(SQL_FROM);
        query.append(Tables.TABLE_PARAM_LISTCOUNT);
        query.append("AS val");
        query.append(SQL_LEFT_JOIN + Tables.TABLE_PARAM_LISTCOUNT_VALUE + "AS dir ON val.param_id=dir.param_id AND val.value=dir.id");
        query.append(SQL_WHERE);
        query.append("val.id=? AND val.param_id=?");

        PreparedStatement ps = con.prepareStatement(query.toString());
        ps.setInt(1, id);
        ps.setInt(2, paramId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            result.add(new IdTitle(rs.getInt(1), rs.getString(2)));
        }
        ps.close();

        return result;
    }

    /**
     * Значения параметра объекта типа 'tree' с текстовыми наименованиями.
     * @param id object ID
     * @param paramId param ID
     * @return
     * @throws SQLException
     */
    @Deprecated
    public List<IdStringTitle> getParamTreeWithTitles(int id, int paramId) throws SQLException {
        log.warnd("getParamTreeWithTitles", "getParamTree");

        List<IdStringTitle> result = new ArrayList<>();

        StringBuilder query = new StringBuilder(200);

        query.append(SQL_SELECT);
        query.append("val.value, dir.title");
        query.append(SQL_FROM);
        query.append(Tables.TABLE_PARAM_TREE);
        query.append("AS val");
        query.append(SQL_LEFT_JOIN + Tables.TABLE_PARAM_TREE_VALUE + "AS dir ON val.param_id=dir.param_id AND val.value=dir.id");
        query.append(SQL_WHERE);
        query.append("val.id=? AND val.param_id=?");
        query.append(SQL_ORDER_BY).append("val.value");

        PreparedStatement ps = con.prepareStatement(query.toString());
        ps.setInt(1, id);
        ps.setInt(2, paramId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            result.add(new IdStringTitle(rs.getString(1), rs.getString(2)));
        }
        ps.close();

        return result;
    }

    /**
     * Use {@link #updateParamFile(int, int, int, FileData)}
     */
    @Deprecated
    public void updateParamFile(int id, int paramId, int position, String comment, FileData fileData) throws Exception {
        updateParamFile(id, paramId, position, fileData);
    }

    @Deprecated
    public void updateParamList(int id, int paramId, Map<Integer, String> values) throws SQLException {
        log.warndMethod("updateParamList", "updateParamListWithComments");
        updateParamListWithComments(id, paramId, values);
    }

    /**
     * Использовать {@link #updateParamListCount(int, int, Map)}.
     */
    @Deprecated
    public void updateParamListCount(int id, int paramId, Map<Integer, Double> values, Map<Integer, String> valuesComments) throws SQLException {
        Map<Integer, BigDecimal> valuesFixed = new HashMap<>(values.size());
        for (Map.Entry<Integer, Double> me : values.entrySet())
            valuesFixed.put(me.getKey(), new BigDecimal(me.getValue()));
        updateParamListCount(id, paramId, valuesFixed);
    }
}
