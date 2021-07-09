package ru.bgcrm.dao;

import static ru.bgcrm.dao.AddressDAO.LOAD_LEVEL_COUNTRY;
import static ru.bgcrm.dao.Tables.TABLE_ADDRESS_HOUSE;
import static ru.bgcrm.dao.Tables.TABLE_ADDRESS_QUARTER;
import static ru.bgcrm.dao.Tables.TABLE_ADDRESS_STREET;
import static ru.bgcrm.dao.Tables.TABLE_FILE_DATE;
import static ru.bgcrm.dao.Tables.TABLE_PARAM_ADDRESS;
import static ru.bgcrm.dao.Tables.TABLE_PARAM_BLOB;
import static ru.bgcrm.dao.Tables.TABLE_PARAM_DATE;
import static ru.bgcrm.dao.Tables.TABLE_PARAM_DATETIME;
import static ru.bgcrm.dao.Tables.TABLE_PARAM_DATETIME_LOG;
import static ru.bgcrm.dao.Tables.TABLE_PARAM_DATE_LOG;
import static ru.bgcrm.dao.Tables.TABLE_PARAM_EMAIL;
import static ru.bgcrm.dao.Tables.TABLE_PARAM_FILE;
import static ru.bgcrm.dao.Tables.TABLE_PARAM_LIST;
import static ru.bgcrm.dao.Tables.TABLE_PARAM_LISTCOUNT;
import static ru.bgcrm.dao.Tables.TABLE_PARAM_LISTCOUNT_VALUE;
import static ru.bgcrm.dao.Tables.TABLE_PARAM_LIST_VALUE;
import static ru.bgcrm.dao.Tables.TABLE_PARAM_PHONE;
import static ru.bgcrm.dao.Tables.TABLE_PARAM_PHONE_ITEM;
import static ru.bgcrm.dao.Tables.TABLE_PARAM_PHONE_LOG;
import static ru.bgcrm.dao.Tables.TABLE_PARAM_PREF;
import static ru.bgcrm.dao.Tables.TABLE_PARAM_TEXT;
import static ru.bgcrm.dao.Tables.TABLE_PARAM_TEXT_LOG;
import static ru.bgcrm.dao.Tables.TABLE_PARAM_TREE;
import static ru.bgcrm.dao.Tables.TABLE_PARAM_TREE_VALUE;
import static ru.bgcrm.model.param.Parameter.LIST_PARAM_USE_DIRECTORY_KEY;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.apache.commons.collections.CollectionUtils;

import ru.bgcrm.cache.ParameterCache;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.FileData;
import ru.bgcrm.model.IdStringTitle;
import ru.bgcrm.model.IdTitle;
import ru.bgcrm.model.IdTitleComment;
import ru.bgcrm.model.param.Parameter;
import ru.bgcrm.model.param.ParameterAddressValue;
import ru.bgcrm.model.param.ParameterEmailValue;
import ru.bgcrm.model.param.ParameterListCountValue;
import ru.bgcrm.model.param.ParameterPhoneValue;
import ru.bgcrm.model.param.ParameterPhoneValueItem;
import ru.bgcrm.model.param.ParameterValuePair;
import ru.bgcrm.util.AddressUtils;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.PreparedDelay;
import ru.bgcrm.util.sql.SQLUtils;
import ru.bgerp.util.Log;

public class ParamValueDAO extends CommonDAO {
    private static final Log log = Log.getLog();

    public static final String DIRECTORY_TYPE_PARAMETER = "parameter";

    private static final String[] TABLE_NAMES = {TABLE_PARAM_DATE, TABLE_PARAM_DATETIME, TABLE_PARAM_PHONE, TABLE_PARAM_PHONE_ITEM, 
            TABLE_PARAM_TEXT, TABLE_PARAM_BLOB, TABLE_PARAM_EMAIL, TABLE_PARAM_LIST, TABLE_PARAM_LIST_VALUE, 
            TABLE_PARAM_TREE, TABLE_PARAM_TREE_VALUE, TABLE_PARAM_LISTCOUNT, TABLE_PARAM_LISTCOUNT_VALUE,  
            TABLE_PARAM_ADDRESS, TABLE_PARAM_FILE};

    private boolean history = true;
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
     * Возвращает адресный параметр объекта.
     * @param id - код объекта.
     * @param paramId - код параметра.
     * @param position - позиция, начиная от 1, если в параметре установлены несколько значений.
     * @return
     * @throws SQLException
     */
    public ParameterAddressValue getParamAddress(int id, int paramId, int position) throws SQLException {
        ParameterAddressValue result = null;

        String query = "SELECT * FROM " + TABLE_PARAM_ADDRESS + "WHERE id=? AND param_id=? AND n=? " + "LIMIT 1";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, id);
        ps.setInt(2, paramId);
        ps.setInt(3, position);

        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            result = getParameterAddressValueFromRs(rs);
        }
        ps.close();

        return result;
    }

    /**
     * Возвращает значения адресного параметра объекта.
     * @param id - код объекта.
     * @param paramId - код параметра.
     * @return ключ - позиция, значение - значение на позиции.
     * @throws SQLException
     */
    public SortedMap<Integer, ParameterAddressValue> getParamAddress(int id, int paramId) throws SQLException {
        return getParamAddressExt(id, paramId, false, null);
    }

    /**
     * Возвращает значения адресного параметра объекта.
     * @param id - код объекта.
     * @param paramId - код параметра.
     * @param loadDirs - признак необходимости загрузить справочники, чтобы был корректно заполнен {@link ParameterAddressValue#getHouse()}/
     * @return ключ - позиция, значение - значение на позиции.
     * @throws SQLException
     */
    public SortedMap<Integer, ParameterAddressValue> getParamAddressExt(int id, int paramId, boolean loadDirs)
            throws SQLException {
        return getParamAddressExt(id, paramId, loadDirs, null);
    }

    /**
     * Возвращает значения адресного параметра объекта.
     * @param id - код объекта.
     * @param paramId - код параметра.
     * @param loadDirs - признак необходимости загрузить справочники, чтобы был корректно заполнен {@link ParameterAddressValue#getHouse()}.
     * @param formatName - наименование формата адреса из конфигурации, с помощью которого форматировать значение адреса.
     * @return ключ - позиция, значение - значение на позиции.
     * @throws SQLException
     */
    public SortedMap<Integer, ParameterAddressValue> getParamAddressExt(int id, int paramId, boolean loadDirs,
            String formatName) throws SQLException {
        SortedMap<Integer, ParameterAddressValue> result = new TreeMap<Integer, ParameterAddressValue>();

        StringBuilder query = new StringBuilder(300);
        query.append("SELECT * FROM " + TABLE_PARAM_ADDRESS + " AS param ");
        if (loadDirs) {
            query.append(" LEFT JOIN " + TABLE_ADDRESS_HOUSE + " AS house ON param.house_id=house.id ");
            AddressDAO.addHouseSelectQueryJoins(query, LOAD_LEVEL_COUNTRY);
        }
        query.append(" WHERE param.id=? AND param.param_id=? ORDER BY param.n");

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

    /**
     * Возвращает значение параметра типа 'file'.
     * @param id код объекта.
     * @param paramId код параметра.
     * @param position номер значения (если значений несколько).
     * @param version номер версии файла.
     * @return
     * @throws SQLException
     */
    public FileData getParamFile(int id, int paramId, int position, int version) throws SQLException {
        FileData result = null;

        String query = "SELECT fd.*,pf.comment,pf.user_id,pf.version,pf.n " + "FROM " + TABLE_PARAM_FILE + " AS pf "
                + "INNER JOIN " + TABLE_FILE_DATE + " AS fd ON pf.value=fd.id "
                + "WHERE pf.id=? AND pf.param_id=? AND pf.n = ? AND pf.version=? " + "LIMIT 1";

        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, id);
        ps.setInt(2, paramId);
        ps.setInt(3, position);
        ps.setInt(4, version);

        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            result = FileDataDAO.getFromRs(rs, "fd.", true);
        }
        ps.close();

        return result;
    }

    /**
     * Возвращает существующие значения параметра типа 'file'
     * @param id код объекта.
     * @param paramId код параметра.
     * @return ключ - строка позиция + '_' + версия, значение - данные.
     * @throws SQLException
     */
    public SortedMap<String, FileData> getParamFile(int id, int paramId) throws SQLException {
        SortedMap<String, FileData> fileMap = new TreeMap<String, FileData>();

        String query = "SELECT fd.*,pf.comment,pf.user_id, pf.n, pf.version " + "FROM " + TABLE_PARAM_FILE
                + " AS pf " + "INNER JOIN " + TABLE_FILE_DATE + " AS fd ON pf.value=fd.id "
                + "WHERE pf.id=? AND pf.param_id=? ";

        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, id);
        ps.setInt(2, paramId);

        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            fileMap.put(rs.getInt("pf.n") + "-" + rs.getInt("pf.version"), FileDataDAO.getFromRs(rs, "fd.", true));
        }
        ps.close();

        return fileMap;
    }

    /**
     * Возвращает значение параметра типа 'date'.
     * @param id код объекта.
     * @param paramId код параметра.
     * @return
     * @throws SQLException
     */
    public Date getParamDate(int id, int paramId) throws SQLException {
        return getParamDate(id, paramId, TABLE_PARAM_DATE);
    }

    /**
     * Возвращает значение параметра типа 'datetime'.
     * @param id код объекта.
     * @param paramId код параметра.
     * @return
     * @throws SQLException
     */
    public Date getParamDateTime(int id, int paramId) throws SQLException {
        return getParamDate(id, paramId, TABLE_PARAM_DATETIME);
    }

    private Date getParamDate(int id, int paramId, String table) throws SQLException {
        Date result = null;

        String query = "SELECT value FROM " + table + " WHERE id=? AND param_id=? LIMIT 1";

        PreparedStatement ps = con.prepareStatement(query.toString());
        ps.setInt(1, id);
        ps.setInt(2, paramId);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            result = TimeUtils.convertTimestampToDate(rs.getTimestamp(1));
        }
        ps.close();

        return result;
    }

    /**
     * Возвращает значение параметра типа 'text'.
     * @param id код объекта.
     * @param paramId код параметра.
     * @return
     * @throws SQLException
     */
    public String getParamText(int id, int paramId) throws SQLException {
        return getTextParam(id, paramId, TABLE_PARAM_TEXT);
    }

    /**
     * Возвращает значение параметра типа 'blob'.
     * @param id код объекта.
     * @param paramId код параметра.
     * @return
     * @throws SQLException
     */
    public String getParamBlob(int id, int paramId) throws SQLException {
        return getTextParam(id, paramId, TABLE_PARAM_BLOB);
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
     * Возвращает значение параметра типа 'email'.
     * @param id код объекта.
     * @param paramId код параметра.
     * @position позиция параметра, если значений несколько.
     * @return
     * @throws SQLException
     */
    public ParameterEmailValue getParamEmail(int id, int paramId, int position) throws SQLException {
        ParameterEmailValue emailItem = null;
            
        String query = "SELECT * FROM " + TABLE_PARAM_EMAIL + "WHERE id=? AND param_id=? AND n=? " + "LIMIT 1";

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
     * Возвращает значения параметра типа 'email'.
     * @param id код объекта.
     * @param paramId код параметра.
     * @return ключ - позиция значения, значение - данные по параметру.
     * @throws SQLException
     */
    public SortedMap<Integer, ParameterEmailValue> getParamEmail(int id, int paramId) throws SQLException {
        SortedMap<Integer, ParameterEmailValue> emailItems = new TreeMap<Integer, ParameterEmailValue>();

        String query = "SELECT * FROM " + TABLE_PARAM_EMAIL + "WHERE id=? AND param_id=? " + "ORDER BY n ";

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
     * Возвращает значения параметра типа 'phone'
     * @param id код объекта.
     * @param paramId код параметра.
     * @return
     * @throws SQLException
     */
    public ParameterPhoneValue getParamPhone(int id, int paramId) throws SQLException {
        boolean notEmpty = false;
        ParameterPhoneValue result = new ParameterPhoneValue();

        String query = " SELECT value FROM " + TABLE_PARAM_PHONE + " WHERE id=? AND param_id=? LIMIT 1";
        PreparedStatement ps = con.prepareStatement(query.toString());
        ps.setInt(1, id);
        ps.setInt(2, paramId);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            notEmpty = true;
        }
        ps.close();

        if (notEmpty) {
            List<ParameterPhoneValueItem> itemList = new ArrayList<ParameterPhoneValueItem>();

            query = " SELECT phone, format, comment, flags FROM " + TABLE_PARAM_PHONE_ITEM
                    + "WHERE id=? AND param_id=? ORDER BY n";
            ps = con.prepareStatement(query.toString());
            ps.setInt(1, id);
            ps.setInt(2, paramId);
            rs = ps.executeQuery();
            while (rs.next()) {
                itemList.add(getParamPhoneValueItemFromRs(rs));
            }
            result.setItemList(itemList);
            ps.close();
        }
        return result;
    }

    /**
     * Возвращает значения параметра типа 'list'.
     * @param id код объекта.
     * @param paramId
     * @return Set с кодами значений.
     * @throws SQLException
     */
    public Set<Integer> getParamList(int id, int paramId) throws SQLException {
        Set<Integer> result = new HashSet<Integer>();

        String query = "SELECT value FROM " + TABLE_PARAM_LIST + "WHERE id=? AND param_id=?";

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
     * Возвращает значения параметра типа 'list' с наименованиями значений.
     * @param id код объекта.
     * @param paramId код параметра.
     * @return
     * @throws SQLException
     */
    public List<IdTitle> getParamListWithTitles(int id, int paramId) throws SQLException {
        List<IdTitleComment> values = getParamListWithTitlesAndComments(id, paramId);
        return new ArrayList<IdTitle>(values);
    }

    /** 
     * Возвращает значения параметра типа 'list' с комментариями значений.
     * @param id код объекта.
     * @param paramId код параметра.
     * @return ключ - код значения, значение - комментарий.
     * @throws SQLException
     */
    public Map<Integer, String> getParamListWithComments(int id, int paramId) throws SQLException {
        Map<Integer, String> result = new LinkedHashMap<Integer, String>();

        String query = "SELECT value, comment FROM " + TABLE_PARAM_LIST + "WHERE id=? AND param_id=?";

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
     * Возвращает значения параметра типа 'list' с наименованиями значений и примечаниями.
     * @param id код объекта.
     * @param paramId код параметра.
     * @return
     * @throws SQLException
     */
    public List<IdTitleComment> getParamListWithTitlesAndComments(int id, int paramId) throws SQLException {
        List<IdTitleComment> result = new ArrayList<IdTitleComment>();

        StringBuilder query = new StringBuilder();

        query.append(SQL_SELECT);
        query.append(" val.value, dir.title, val.comment ");
        query.append(SQL_FROM);
        query.append(TABLE_PARAM_LIST);
        query.append(" AS val ");
        addListParamJoin(query, paramId);
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
     * Возвращает значения параметра типа 'listcount' с наименованиями значений.
     * @param id код объекта.
     * @param paramId код параметра.
     * @return
     * @throws SQLException
     */
    public List<IdTitle> getParamListCountWithTitles(int id, int paramId) throws SQLException {
        List<IdTitle> result = new ArrayList<IdTitle>();

        StringBuilder query = new StringBuilder();

        query.append(SQL_SELECT);
        query.append("val.value, dir.title");
        query.append(SQL_FROM);
        query.append(TABLE_PARAM_LISTCOUNT);
        query.append("AS val");
        addListCountParamJoin(query, paramId);
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
     * Возвращает значения параметра типа 'listcount'.
     * @param id код объекта.
     * @param paramId код параметра.
     * @return ключ - код значения, значение - доп. данные. 
     * @throws SQLException
     */
    public Map<Integer, ParameterListCountValue> getParamListCount(int id, int paramId) throws SQLException {
        Map<Integer, ParameterListCountValue> result = new HashMap<Integer, ParameterListCountValue>();

        String query = "SELECT value,count,comment FROM " + TABLE_PARAM_LISTCOUNT + "WHERE id=? AND param_id=?";

        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, id);
        ps.setInt(2, paramId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            result.put(rs.getInt(1), new ParameterListCountValue(rs.getBigDecimal(2), rs.getString(3)));
        }
        ps.close();

        return result;
    }

    /**
     * Возвращает значения параметра типа 'tree'.
     * @param id код объекта.
     * @param paramId код параметра.
     * @return набор значений.
     * @throws SQLException
     */
    public Set<String> getParamTree(int id, int paramId) throws SQLException {
        Set<String> result = new HashSet<String>();

        String query = "SELECT value FROM " + TABLE_PARAM_TREE + "WHERE id=? AND param_id=?";

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
     * Устанавливает значение параметра типа 'text'.
     * @param id код объекта.
     * @param paramId код параметра.
     * @param value значение, null или пустая строка - удалить значение.
     * @throws SQLException
     */
    public void updateParamText(int id, int paramId, String value) throws SQLException {
        if (Utils.isBlankString(value)) {
            value = null;
        }

        updateSimpleParam(id, paramId, value, TABLE_PARAM_TEXT, TABLE_PARAM_TEXT_LOG);

        if (history) {
            logParam(id, paramId, userId, value);
        }
    }

    /**
     * Устанавливает значение параметра типа 'blob'.
     * @param id код объекта.
     * @param paramId код параметра.
     * @param value значение, null или пустая строка - удалить значение.
     * @throws SQLException
     */
    public void updateParamBlob(int id, int paramId, String value) throws SQLException {
        if (Utils.isBlankString(value)) {
            value = null;
        }

        updateSimpleParam(id, paramId, value, TABLE_PARAM_BLOB, TABLE_PARAM_BLOB);

        if (history) {
            logParam(id, paramId, userId, value != null ? "Длина: " + value.length() : null);
        }
    }

    /**
     * Обновляет/добавляет/удаляет значения параметра типа EMail.
     * @param id - код сущности в БД.
     * @param paramId - код параметра.
     * @param position - позиция значения, начинается с 1, 0 - добавить новое значение с позицией MAX+1.
     * @param value - значение, null - удаление параметра на указанной позиции, если position>0; иначе - удаление всех значений.
     * @throws SQLException
     */
    public void updateParamEmail(int id, int paramId, int position, ParameterEmailValue value) throws SQLException {
        int index = 1;

        if (value == null) {
            PreparedDelay psDelay = new PreparedDelay(con);

            psDelay.addQuery(SQL_DELETE + TABLE_PARAM_EMAIL + SQL_WHERE + "id=? AND param_id=?");
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

                String query = "SELECT MAX(n) + 1 FROM " + TABLE_PARAM_EMAIL + " WHERE id=? AND param_id=?";
                PreparedStatement ps = con.prepareStatement(query);
                ps.setInt(1, id);
                ps.setInt(2, paramId);

                ResultSet rs = ps.executeQuery();
                if (rs.next() && rs.getObject(1) != null) {
                    position = rs.getInt(1);
                }
                ps.close();

                query = "INSERT INTO " + TABLE_PARAM_EMAIL + " SET id=?, param_id=?, n=?, value=?, comment=?";
                ps = con.prepareStatement(query);
                ps.setInt(index++, id);
                ps.setInt(index++, paramId);
                ps.setInt(index++, position);
                ps.setString(index++, value.getValue());
                ps.setString(index++, value.getComment());

                ps.executeUpdate();

                ps.close();
            } else {
                String query = "UPDATE " + TABLE_PARAM_EMAIL + " SET value=?, comment=?"
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

        // Лог изменений.
        if (history) {
            logParam(id, paramId, userId, ParameterEmailValue
                    .getEmails(new ArrayList<ParameterEmailValue>(getParamEmail(id, paramId).values())));
        }
    }

    /**
     * Устанавливает значения параметра типа 'list' с примечаниями.
     * @param id код объекта.
     * @param paramId код параметра.
     * @param values ключ - значение параметра, значение - текстовое примечание.
     * @throws SQLException
     */
    public void updateParamList(int id, int paramId, Map<Integer, String> values) throws SQLException {
        deleteFromParamTable(id, paramId, TABLE_PARAM_LIST);

        String query = "INSERT INTO " + TABLE_PARAM_LIST + "(id, param_id, value, comment) VALUES (?,?,?,?)";

        PreparedStatement ps = con.prepareStatement(query.toString());
        ps.setInt(1, id);
        ps.setInt(2, paramId);
        for (Map.Entry<Integer, String> value : values.entrySet()) {
            ps.setInt(3, value.getKey());
            ps.setString(4, value.getValue());
            ps.executeUpdate();
        }
        ps.close();

        // change log
        if (history) {
            logParam(id, paramId, userId, Utils.getObjectTitles(getParamListWithTitles(id, paramId)));
        }
    }

    /**
     * Устанавливает значения параметра типа 'list' с пустыми примечениями.
     * @param id код объекта.
     * @param paramId код параметра.
     * @param values набор с кодами значений.
     * @throws SQLException
     */
    public void updateParamList(int id, int paramId, Set<Integer> values) throws SQLException {
        deleteFromParamTable(id, paramId, TABLE_PARAM_LIST);

        String query = "INSERT INTO " + TABLE_PARAM_LIST + "(id, param_id, value) VALUES (?,?,?)";

        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, id);
        ps.setInt(2, paramId);
        for (int value : values) {
            ps.setInt(3, value);
            ps.executeUpdate();
        }
        ps.close();

        // change log
        if (history) {
            logParam(id, paramId, userId, Utils.getObjectTitles(getParamListWithTitles(id, paramId)));
        }
    }

    /**
     * Устанавливает значения параметра типа 'listcount'.
     * @param id код объекта.
     * @param paramId код параметра.
     * @param values значения, ключ - код значение, значение - количество.
     * @throws SQLException
     */
    public void updateParamListCount(int id, int paramId, Map<Integer, BigDecimal> values) throws SQLException {
        List<IdTitle> existIdTitles = getParamListCountWithTitles(id, paramId);

        PreparedStatement ps = null;

        if (existIdTitles.size() > 0) {
            deleteFromParamTable(id, paramId, TABLE_PARAM_LISTCOUNT);
        }

        String query = "INSERT INTO " + TABLE_PARAM_LISTCOUNT
                + "(id, param_id, value, count, comment) VALUES (?,?,?,?,?)";

        ps = con.prepareStatement(query.toString());
        ps.setInt(1, id);
        ps.setInt(2, paramId);
        for (Integer value : values.keySet()) {
            ps.setInt(3, value);
            ps.setBigDecimal(4, values.get(value));
            ps.setString(5, /*valuesComments.get( value )*/ "");
            ps.executeUpdate();
        }
        ps.close();

        // Лог изменений.
        if (history) {
            logParam(id, paramId, userId, Utils.getObjectTitles(getParamListWithTitles(id, paramId)));
        }
    }

    /**
     * Использовать {@link #updateParamListCount(int, int, Map)}.
     */
    @Deprecated
    public void updateParamListCount(int id, int paramId, Map<Integer, Double> values,
            Map<Integer, String> valuesComments) throws SQLException {
        Map<Integer, BigDecimal> valuesFixed = new HashMap<>(values.size());
        for (Map.Entry<Integer, Double> me : values.entrySet())
            valuesFixed.put(me.getKey(), new BigDecimal(me.getValue()));
        updateParamListCount(id, paramId, valuesFixed);
    }

    /**
     * Устанавливает значение параметра типа 'file' на позицию с новой версией.
     * @param id код объекта.
     * @param paramId код параметра.
     * @param position позиция.
     * @param version версия файла.
     * @param comment примечение.
     * @param fileData значение параметра данной версии, если null - удаление значения с позиции.
     * @throws Exception
     */
    public void updateParamFile(int id, int paramId, int position, int version, String comment, FileData fileData)
            throws Exception {
        //TODO: При position=-1 - сделать удаление всех значений параметра, как в #updateParameterEmail.
        if (fileData == null) {
            FileData currentValue = getParamFile(id, paramId, position, version);
            if (currentValue != null) {

                String query = "DELETE FROM " + TABLE_PARAM_FILE
                        + " WHERE id=? AND param_id=? AND n=? AND version=?";

                PreparedStatement ps = con.prepareStatement(query);
                ps.setInt(1, id);
                ps.setInt(2, paramId);
                ps.setInt(3, position);
                ps.setInt(4, version);
                ps.executeUpdate();
                ps.close();

                //проверка на оставшиеся ссылки на файл
                query = "SELECT COUNT(*) FROM " + TABLE_PARAM_FILE + " AS pf " + " LEFT JOIN " + TABLE_FILE_DATE
                        + " AS fd ON fd.id = pf.value " + " WHERE fd.secret =? ";
                ps = con.prepareStatement(query);
                ps.setString(1, currentValue.getSecret());

                ResultSet rs = ps.executeQuery();
                int count = 0;
                if (rs.next()) {
                    count = rs.getInt(1);
                }

                if (count == 0) {
                    new FileDataDAO(con).delete(currentValue);
                }

                ps.close();
            }
        } else {
            version = 1;
            position = 0;
            String query = " SELECT MAX(version)+1,n " + " FROM " + TABLE_PARAM_FILE + " AS pf " + " LEFT JOIN "
                    + TABLE_FILE_DATE + " AS fd on fd.id = pf.value " + " WHERE pf.id = ? AND fd.title = ? ";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, id);
            ps.setString(2, fileData.getTitle());

            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getObject(1) != null) {
                position = rs.getInt(2);
                version = rs.getInt(1);
            }
            ps.close();

            if (position == 0) {
                query = "SELECT MAX(n) + 1 FROM " + TABLE_PARAM_FILE + " WHERE id=? AND param_id=?";
                ps = con.prepareStatement(query);
                ps.setInt(1, id);
                ps.setInt(2, paramId);

                rs = ps.executeQuery();
                if (rs.next() && rs.getObject(1) != null) {
                    position = rs.getInt(1);
                } else {
                    position = 1;
                }
                ps.close();
            }

            query = "INSERT INTO " + TABLE_PARAM_FILE
                    + "(id, param_id, n, value, user_id, comment,version) VALUES (?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement insertPs = con.prepareStatement(query);
            insertPs.setInt(1, id);
            insertPs.setInt(2, paramId);
            insertPs.setInt(3, position);
            insertPs.setInt(4, fileData.getId());

            insertPs.setInt(5, userId);
            insertPs.setString(6, comment);
            insertPs.setInt(7, version);

            insertPs.executeUpdate();
            insertPs.close();

            //TODO: Разобраться с логированием.
            if (history) {
                String fileName = fileData.getTitle();
                ParamLogDAO paramLogDAO = new ParamLogDAO(this.con);
                paramLogDAO.insertParamLog(id, paramId, userId, fileName);
            }
        }
    }

    /**
     * Обновляет/добавляет/удаляет значения адресного параметра.
     * @param id - код сущности в БД.
     * @param paramId - код параметра.
     * @param position - позиция значения, начинается с 1, 0 - добавить новое значение с позицией MAX+1.
     * @param value - значение, null - удаление параметра на указанной позиции, если position>0; иначе - удаление всех значений.
     * @throws SQLException
     */
    public void updateParamAddress(int id, int paramId, int position, ParameterAddressValue value) throws SQLException {
        int index = 1;

        if (value == null) {
            PreparedDelay psDelay = new PreparedDelay(con);

            psDelay.addQuery("DELETE FROM " + TABLE_PARAM_ADDRESS + " WHERE id=? AND param_id=? ");
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

                String query = "SELECT MAX(n) + 1 FROM " + TABLE_PARAM_ADDRESS + " WHERE id=? AND param_id=?";
                PreparedStatement ps = con.prepareStatement(query);
                ps.setInt(1, id);
                ps.setInt(2, paramId);

                ResultSet rs = ps.executeQuery();
                if (rs.next() && rs.getObject(1) != null) {
                    position = rs.getInt(1);
                }
                ps.close();

                insertParamAddress(id, paramId, position, value);
            } else {
                String query = "UPDATE " + TABLE_PARAM_ADDRESS
                        + " SET value=?, house_id=?, flat=?, room=?, pod=?, floor=?, comment=?, custom=?"
                        + " WHERE id=? AND param_id=? AND n=?";
                PreparedStatement ps = con.prepareStatement(query);

                ps.setString(index++, value.getValue());
                ps.setInt(index++, value.getHouseId());
                ps.setString(index++, value.getFlat());
                ps.setString(index++, value.getRoom());
                ps.setInt(index++, value.getPod());
                ps.setInt(index++, value.getFloor());
                ps.setString(index++, value.getComment());
                ps.setString(index++, value.getCustom());
                ps.setInt(index++, id);
                ps.setInt(index++, paramId);
                ps.setInt(index++, position);

                int cnt = ps.executeUpdate();

                ps.close();
                
                if (cnt == 0)
                    insertParamAddress(id, paramId, position, value);
            }
        }

        // Лог изменений.
        if (history) {
            StringBuffer result = new StringBuffer();
            SortedMap<Integer, ParameterAddressValue> addresses = getParamAddress(id, paramId);
            Iterator<Integer> it = addresses.keySet().iterator();
            while (it.hasNext()) {
                if (result.length() > 0) {
                    result.append(" ");
                }
                Integer key = it.next();
                result.append(addresses.get(key).getValue());
                result.append(";");
            }
            logParam(id, paramId, userId, result.toString());
        }
    }

    private void insertParamAddress(int id, int paramId, int position, ParameterAddressValue value) throws SQLException {
        int index = 1;
        
        String query = "INSERT INTO " + TABLE_PARAM_ADDRESS
                + " SET id=?, param_id=?, n=?, value=?, house_id=?, flat=?, room=?, pod=?, floor=?, comment=?, custom=?";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(index++, id);
        ps.setInt(index++, paramId);
        ps.setInt(index++, position);
        ps.setString(index++, value.getValue());
        ps.setInt(index++, value.getHouseId());
        ps.setString(index++, value.getFlat());
        ps.setString(index++, value.getRoom());
        ps.setInt(index++, value.getPod());
        ps.setInt(index++, value.getFloor());
        ps.setString(index++, value.getComment());
        ps.setString(index++, value.getCustom());

        ps.executeUpdate();

        ps.close();
    }

    /**
     * Обновляет строки адресных параметров для дома. Используется после изменений в адресных справочников,
     * для генерации корректных строк с адресными параметрами.
     * @param houseId код дома.
     * @throws SQLException
     */
    public void updateParamsAddressOnHouseUpdate(int houseId) throws SQLException {
        String query = "SELECT * FROM " + TABLE_PARAM_ADDRESS + " WHERE house_id=?";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, houseId);

        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            int id = rs.getInt("id");
            int paramId = rs.getInt("param_id");
            int pos = rs.getInt("n");

            ParameterAddressValue value = getParameterAddressValueFromRs(rs);
            value.setValue(AddressUtils.buildAddressValue(value, con));

            updateParamAddress(id, paramId, pos, value);
        }

        ps.close();
    }

    /**
     * Устанавливает значения параметра типа 'phone'.
     * @param id код объекта.
     * @param paramId код параметра.
     * @param value значения, null либо пустой itemList - удаление значения.
     * @throws SQLException
     */
    public void updateParamPhone(int id, int paramId, ParameterPhoneValue value) throws SQLException {
        if (value == null || value.getItemList().size() == 0) {
            deleteFromParamTable(id, paramId, TABLE_PARAM_PHONE);
            deleteFromParamTable(id, paramId, TABLE_PARAM_PHONE_ITEM);
        } else {
            String newPhones = ParameterPhoneValueItem.getPhones(value.getItemList());

            updateSimpleParam(id, paramId, newPhones, TABLE_PARAM_PHONE, TABLE_PARAM_PHONE_LOG);

            deleteFromParamTable(id, paramId, TABLE_PARAM_PHONE_ITEM);

            int index = 1;

            String query = "INSERT INTO" + TABLE_PARAM_PHONE_ITEM
                    + "SET id=?, param_id=?, n=?, phone=?, format=?, comment=?, flags=?";
            PreparedStatement ps = con.prepareStatement(query.toString());
            ps.setInt(index++, id);
            ps.setInt(index++, paramId);

            int n = 1;
            for (ParameterPhoneValueItem item : value.getItemList()) {
                index = 3;
                ps.setInt(index++, n++);
                ps.setString(index++, item.getPhone());
                ps.setString(index++, item.getFormat());
                ps.setString(index++, item.getComment());
                ps.setInt(index++, item.getFlags());
                ps.executeUpdate();
            }
            ps.close();
        }

        if (history) {
            String newValue = null;

            ParameterPhoneValue newPhones = getParamPhone(id, paramId);
            if (newPhones != null) {
                newValue = ParameterPhoneValueItem.getPhones(newPhones.getItemList());
            }

            logParam(id, paramId, userId, newValue);
        }
    }

    /**
     * Устанавливает значение параметра типа 'date'.
     * @param id код объекта.
     * @param paramId код параметра.
     * @param value значение, null - удаление.
     * @throws SQLException
     */
    public void updateParamDate(int id, int paramId, Date value) throws SQLException {
        updateSimpleParam(id, paramId, value, TABLE_PARAM_DATE, TABLE_PARAM_DATE_LOG);

        if (history) {
            logParam(id, paramId, userId, TimeUtils.format(value, TimeUtils.FORMAT_TYPE_YMD));
        }
    }

    /**
     * Устанавливает значение параметра типа 'datetime'.
     * @param id код объекта.
     * @param paramId код параметра.
     * @param value значение, null - удаление.
     * @throws SQLException
     */
    public void updateParamDateTime(int id, int paramId, Date value) throws SQLException {
        updateSimpleParam(id, paramId, value, TABLE_PARAM_DATETIME, TABLE_PARAM_DATETIME_LOG);

        if (history) {
            logParam(id, paramId, userId, TimeUtils.format(value, TimeUtils.FORMAT_TYPE_YMDHMS));
        }
    }

    /**
     * Удаляет параметры объекта.
     * @param objectType тип объекта.
     * @param id код объекта.
     * @throws SQLException
     */
    public void deleteParams(String objectType, int id) throws SQLException {
        //TODO: Добавить удаление лога изменения параметров
        for (String tableName : TABLE_NAMES) {
            StringBuilder query = new StringBuilder();
            query.append("DELETE param FROM ");
            query.append(tableName);
            query.append(" AS param");
            query.append(SQL_INNER_JOIN);
            query.append(TABLE_PARAM_PREF);
            query.append("AS pref ON param.param_id=pref.id AND pref.object=?");
            query.append(SQL_WHERE);
            query.append("param.id=?");

            PreparedStatement ps = con.prepareStatement(query.toString());
            ps.setString(1, objectType);
            ps.setInt(2, id);
            ps.executeUpdate();
            ps.close();
        }
    }

    /**
     * Переносит параметры при с кода объекта на -код объекта.
     * Используется при преобразовании не созданного до конца процесса с отрицательным кодом в созданный.
     * @param objectType
     * @param currentObjectId
     * @throws SQLException
     */
    public void objectIdInvert(String objectType, int currentObjectId) throws SQLException {
        for (String tableName : TABLE_NAMES) {
            StringBuilder query = new StringBuilder();
            query.append("UPDATE ");
            query.append(tableName);
            query.append(" AS param");
            query.append(SQL_INNER_JOIN);
            query.append(TABLE_PARAM_PREF);
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

        StringTokenizer st = new StringTokenizer(copyMapping, ";,");
        while (st.hasMoreTokens()) {
            String token = st.nextToken();

            String[] pair = token.split(":");
            if (pair.length == 2) {
                copyParam(fromObjectId, Utils.parseInt(pair[0]), toObjectId, Utils.parseInt(pair[1]));
            } else if (Utils.parseInt(token) > 0) {
                int paramId = Utils.parseInt(token);
                copyParam(fromObjectId, paramId, toObjectId, paramId);
            } else {
                log.error("Incorrect copy param mapping: " + token);
            }
        }
    }

    /**
     * Копирует параметры с объекта на объект
     * @param fromObjectId код объекта исходного.
     * @param toObjectId код объекта целевого.
     * @param paramIds коды параметров.
     * @throws SQLException, BGException
     */
    public void copyParams(int fromObjectId, int toObjectId, Collection<Integer> paramIds) throws SQLException, BGException {
        for (int paramId : paramIds) {
            copyParam(fromObjectId, paramId, toObjectId, paramId);
        }
    }

    /**
     * Копирует параметр с объекта на объект.
     * @param fromObjectId код объекта исходного.
     * @param toObjectId код объекта целевого.
     * @param paramId коды параметра.
     * @throws SQLException, BGException
     */
    public void copyParam(int fromObjectId, int toObjectId, int paramId) throws SQLException, BGException {
        copyParam(fromObjectId, paramId, toObjectId, paramId);
    }

    /**
     * Копирует параметр с объекта на объект. Параметры должны быть одного типа.
     * @param fromObjectId код объекта исходного.
     * @param fromParamId код параметра исходного.
     * @param toObjectId код объекта целевого
     * @param toParamId код параметра целевого.
     * @throws SQLException, BGException
     */
    public void copyParam(int fromObjectId, int fromParamId, int toObjectId, int toParamId) throws SQLException, BGException {
        String query = null;
        ArrayList<PreparedStatement> psList = new ArrayList<PreparedStatement>();

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

        final String paramType = paramFrom.getType();

        // адрес
        if (Parameter.TYPE_ADDRESS.equals(paramType)) {
            query = "INSERT INTO " + TABLE_PARAM_ADDRESS
                    + " (id, param_id, n, house_id, flat, room, pod, floor, value, comment, custom) "
                    + "SELECT ?, ?, n, house_id, flat, room, pod, floor, value, comment, custom " + "FROM "
                    + TABLE_PARAM_ADDRESS + " WHERE id=? AND param_id=?";
            psList.add(con.prepareStatement(query));
        }
        // E-Mail
        else if (Parameter.TYPE_EMAIL.equals(paramType)) {
            query = "INSERT INTO " + TABLE_PARAM_EMAIL + " (id, param_id, n, value) " + "SELECT ?, ?, n, value "
                    + "FROM " + TABLE_PARAM_EMAIL + " WHERE id=? AND param_id=?";
            psList.add(con.prepareStatement(query));
        }
        // list
        else if (Parameter.TYPE_LIST.equals(paramType)) {
            query = "INSERT IGNORE INTO " + TABLE_PARAM_LIST + "(id, param_id, value) " + "SELECT ?, ?, value " + "FROM "
                    + TABLE_PARAM_LIST + " WHERE id=? AND param_id=?";
            psList.add(con.prepareStatement(query));
        }
        // listcount
        else if (Parameter.TYPE_LISTCOUNT.equals(paramType)) {
            query = "INSERT INTO " + TABLE_PARAM_LISTCOUNT + "(id, param_id, value, count) "
                    + "SELECT ?, ?, value, count " + "FROM " + TABLE_PARAM_LISTCOUNT + " WHERE id=? AND param_id=?";
            psList.add(con.prepareStatement(query));
        }
        // tree
        else if (Parameter.TYPE_TREE.equals(paramType)) {
            query = "INSERT INTO " + TABLE_PARAM_TREE + "(id, param_id, value) " + "SELECT ?, ?, value " + "FROM "
                    + TABLE_PARAM_TREE + " WHERE id=? AND param_id=?";
            psList.add(con.prepareStatement(query));
        }
        // дата, дата+время, текст, блоб, телефон
        else if (Parameter.TYPE_DATE.equals(paramType) || Parameter.TYPE_DATETIME.equals(paramType)
                || Parameter.TYPE_TEXT.equals(paramType) || Parameter.TYPE_BLOB.equals(paramType)
                || Parameter.TYPE_PHONE.equals(paramType)) {
            String tableName = "param_" + paramType;

            query = "INSERT INTO " + tableName + " (id, param_id, value) " + "SELECT ?, ?, value " + "FROM "
                    + tableName + " WHERE id=? AND param_id=?";
            psList.add(con.prepareStatement(query));

            if (Parameter.TYPE_PHONE.equals(paramType)) {
                query = "INSERT INTO " + TABLE_PARAM_PHONE_ITEM
                        + " (id, param_id, n, phone, format, comment, flags) "
                        + "SELECT ?, ?, n, phone, format, comment, flags " + "FROM " + TABLE_PARAM_PHONE_ITEM
                        + " WHERE id=? AND param_id=?";
                psList.add(con.prepareStatement(query));
            }
        } else if (Parameter.TYPE_FILE.equals(paramType)) {
            query = "INSERT INTO " + TABLE_PARAM_FILE + "(id, param_id, n,value,user_id,comment,version) "
                    + "SELECT ?, ?, n,value,user_id,comment,version " + "FROM " + TABLE_PARAM_FILE
                    + " WHERE id=? AND param_id=?";
            psList.add(con.prepareStatement(query));
        } else {
            throw new BGException("Param type not supported for copy yet.");
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
     * Проверяет заполненость параметра для объекта с кодом id.
     * @param id код объекта.
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
     * Загрузка значений в список параметров.
     * @param id
     * @param paramList
     * @throws Exception
     */
    public List<ParameterValuePair> loadParameters(List<Parameter> paramList, int id, boolean offEncription)
            throws Exception {
        Map<String, List<Integer>> paramTypeMap = new HashMap<String, List<Integer>>();

        List<ParameterValuePair> result = new ArrayList<ParameterValuePair>(paramList.size());
        Map<Integer, ParameterValuePair> paramMap = new HashMap<Integer, ParameterValuePair>(paramList.size());

        for (Parameter parameter : paramList) {
            String type = parameter.getType();
            List<Integer> ids = paramTypeMap.get(type);
            if (ids == null) {
                paramTypeMap.put(type, ids = new ArrayList<Integer>());
            }
            ids.add(parameter.getId());

            ParameterValuePair pvp = new ParameterValuePair(parameter);
            paramMap.put(parameter.getId(), pvp);

            result.add(pvp);
        }

        for (String type : paramTypeMap.keySet())
            updateParamValueMap(paramMap, type, paramTypeMap.get(type), id, offEncription);

        return result;
    }

    @Deprecated
    public void loadParameterValue(ParameterValuePair param, int objectId, boolean offEncription) throws Exception {
        Parameter parameter = param.getParameter();
        updateParamValueMap(Collections.singletonMap(parameter.getId(), param), parameter.getType(),
                Collections.singletonList(parameter.getId()), objectId, offEncription);
    }

    /**
     * Загрузка значений параметров определённого типа.
     * @param paramValueMap 
     * @param type
     * @param ids
     * @param objectId
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    private void updateParamValueMap(Map<Integer, ParameterValuePair> paramMap, String type, Collection<Integer> ids,
            int objectId, boolean offEncription) throws Exception {
        StringBuilder query = new StringBuilder();
        
        ResultSet rs = null;
        PreparedStatement ps = null;
        
        if (Parameter.TYPE_LIST.equals(type)) {
            // ключ - имя таблицы справочника, значение - перечень параметров
            Map<String, Set<Integer>> tableParamsMap = new HashMap<String, Set<Integer>>();
            for (Integer paramId : ids) {
                Parameter param = ParameterCache.getParameter(paramId);
                String tableName = param.getConfigMap().get(LIST_PARAM_USE_DIRECTORY_KEY);
                if (tableName == null) {
                    tableName = TABLE_PARAM_LIST_VALUE;
                }

                Set<Integer> pids = tableParamsMap.get(tableName);
                if (pids == null) {
                    tableParamsMap.put(tableName, pids = new HashSet<Integer>());
                }
                pids.add(paramId);
            }

            final String standartPrefix = "SELECT val.param_id, val.value, dir.title, val.comment FROM "
                    + TABLE_PARAM_LIST + " AS val ";
            for (Map.Entry<String, Set<Integer>> me : tableParamsMap.entrySet()) {
                String tableName = me.getKey();
                if (query.length() > 0) {
                    query.append("\nUNION ");
                }

                query.append(standartPrefix);
                addListTableJoin(query, tableName);
                query.append(SQL_WHERE);
                query.append("val.id=" + objectId + " AND val.param_id IN (");
                query.append(Utils.toString(me.getValue()));
                query.append(")");
            }

            ps = con.prepareStatement(query.toString());
            rs = ps.executeQuery();
        } else if (Parameter.TYPE_LISTCOUNT.equals(type)) {
            // ключ - имя таблицы справочника, значение - перечень параметров
            Map<String, Set<Integer>> tableParamsMap = new HashMap<String, Set<Integer>>();
            for (Integer paramId : ids) {
                Parameter param = ParameterCache.getParameter(paramId);
                String tableName = param.getConfigMap().get(LIST_PARAM_USE_DIRECTORY_KEY);
                if (tableName == null) {
                    tableName = TABLE_PARAM_LISTCOUNT_VALUE;
                }

                Set<Integer> pids = tableParamsMap.get(tableName);
                if (pids == null) {
                    tableParamsMap.put(tableName, pids = new HashSet<Integer>());
                }
                pids.add(paramId);
            }

            final String standartPrefix = "SELECT val.param_id, val.value, val.count, dir.title FROM "
                    + TABLE_PARAM_LISTCOUNT + " AS val ";
            for (Map.Entry<String, Set<Integer>> me : tableParamsMap.entrySet()) {
                String tableName = me.getKey();
                if (query.length() > 0) {
                    query.append("\nUNION ");
                }

                query.append(standartPrefix);
                addListCountTableJoin(query, tableName);
                query.append(SQL_WHERE);
                query.append("val.id=" + objectId + " AND val.param_id IN (");
                query.append(Utils.toString(me.getValue()));
                query.append(")");
            }

            ps = con.prepareStatement(query.toString());
            rs = ps.executeQuery();
        } else if (Parameter.TYPE_TREE.equals(type)) {
            // ключ - имя таблицы справочника, значение - перечень параметров
            Map<String, Set<Integer>> tableParamsMap = new HashMap<String, Set<Integer>>();
            for (Integer paramId : ids) {
                Parameter param = ParameterCache.getParameter(paramId);
                String tableName = param.getConfigMap().get(LIST_PARAM_USE_DIRECTORY_KEY);
                if (tableName == null) {
                    tableName = TABLE_PARAM_TREE_VALUE;
                }

                Set<Integer> pids = tableParamsMap.get(tableName);
                if (pids == null) {
                    tableParamsMap.put(tableName, pids = new HashSet<Integer>());
                }
                pids.add(paramId);
            }

            final String standartPrefix = "SELECT val.param_id, val.value, dir.title FROM " + TABLE_PARAM_TREE
                    + " AS val ";
            for (Map.Entry<String, Set<Integer>> me : tableParamsMap.entrySet()) {
                String tableName = me.getKey();
                if (query.length() > 0) {
                    query.append("\nUNION ");
                }

                query.append(standartPrefix);
                addTreeTableJoin(query, tableName);
                query.append(SQL_WHERE);
                query.append("val.id=" + objectId + " AND val.param_id IN (");
                query.append(Utils.toString(me.getValue()));
                query.append(")");
            }

            ps = con.prepareStatement(query.toString());
            rs = ps.executeQuery();
        } else if (Parameter.TYPE_ADDRESS.equals(type)) {
            query.append("SELECT param_id, n, value, house_id FROM param_");
            query.append(type);
            query.append(" WHERE id=? AND param_id IN ( ");
            query.append(Utils.toString(ids));
            query.append(" ) ORDER BY n ");
        } else if (Parameter.TYPE_EMAIL.equals(type)) {
            query.append("SELECT param_id, n, value, comment FROM param_");
            query.append(type);
            query.append(" WHERE id=? AND param_id IN ( ");
            query.append(Utils.toString(ids));
            query.append(" )");
        } else if (Parameter.TYPE_FILE.equals(type)) {
            query.append("SELECT pf.param_id, pf.n, pf.version, pf.user_id,pf.comment,fd.* FROM " + TABLE_PARAM_FILE
                    + " AS pf INNER JOIN " + TABLE_FILE_DATE + " AS fd ON pf.value=fd.id "
                    + " WHERE pf.id=? AND pf.param_id IN ( ");
            query.append(Utils.toString(ids));
            query.append(" ) ORDER BY n,version");
        } else if (Parameter.TYPE_PHONE.equals(type)) {
            query.append("SELECT pi.param_id, pi.n, pi.phone, pi.format, pi.comment, pi.flags FROM " + TABLE_PARAM_PHONE_ITEM 
                    + "AS pi WHERE pi.id=? AND pi.param_id IN ( ");
            query.append(Utils.toString(ids));
            query.append(" ) ORDER BY pi.n");			    
        } else {
            query.append("SELECT param_id, value FROM param_");
            query.append(type);
            query.append(" WHERE id=? AND param_id IN ( ");
            query.append(Utils.toString(ids));
            query.append(" )");				
        }
        
        if (ps == null) {
            ps = con.prepareStatement(query.toString());
            ps.setInt(1, objectId);
            rs = ps.executeQuery();
        }

        while (rs.next()) {
            final int paramId = rs.getInt(1);

            ParameterValuePair param = paramMap.get(paramId);

            if (Parameter.TYPE_DATE.equals(type)) {
                param.setValue(rs.getDate(2));
            } else if (Parameter.TYPE_DATETIME.equals(type)) {
                param.setValue(TimeUtils.convertTimestampToDate(rs.getTimestamp(2)));
            } else if (Parameter.TYPE_LIST.equals(type)) {
                List<IdTitle> values = (List<IdTitle>) param.getValue();
                if (values == null)
                    param.setValue(values = new ArrayList<IdTitle>());

                IdTitle value = new IdTitle(rs.getInt(2), rs.getString(3));

                String comment = rs.getString(4);
                // TODO: IdTitleComment?
                if (Utils.notBlankString(comment))
                    value.setTitle(value.getTitle() + " [" + comment + "]");

                values.add(value);
            } else if (Parameter.TYPE_LISTCOUNT.equals(type)) {
                List<IdTitle> values = (List<IdTitle>) param.getValue();
                if (values == null)
                    param.setValue(values = new ArrayList<IdTitle>());
                values.add(new IdTitle(rs.getInt(2),
                        rs.getString(4) + ":" + rs.getBigDecimal(3).stripTrailingZeros().toPlainString()));
            } else if (Parameter.TYPE_TREE.equals(type)) {
                List<IdStringTitle> values = (List<IdStringTitle>) param.getValue();
                if (values == null)
                    param.setValue(values = new ArrayList<>());
                values.add(new IdStringTitle(rs.getString(2), rs.getString(3)));
            } else if (Parameter.TYPE_ADDRESS.equals(type)) {
                Map<Integer, ParameterAddressValue> values = (Map<Integer, ParameterAddressValue>) param.getValue();
                if (values == null)
                    param.setValue(values = new TreeMap<Integer, ParameterAddressValue>());

                ParameterAddressValue val = new ParameterAddressValue();
                val.setValue(rs.getString(3));
                val.setHouseId(rs.getInt(4));

                values.put(rs.getInt(2), val);
            } else if (Parameter.TYPE_EMAIL.equals(type)) {
                Map<Integer, String> values = (Map<Integer, String>) param.getValue();
                if (values == null)
                    param.setValue(values = new TreeMap<Integer, String>());

                if (!"".equals(rs.getString(4))) {
                    values.put(rs.getInt(2), rs.getString(3) + " [ " + rs.getString(4) + " ]");
                } else {
                    values.put(rs.getInt(2), rs.getString(3));
                }
            } else if (Parameter.TYPE_FILE.equals(type)) {
                Map<String, FileData> values = (Map<String, FileData>) param.getValue();
                if (values == null)
                    param.setValue(values = new LinkedHashMap<String, FileData>());

                values.put(rs.getString(2) + rs.getString(3), FileDataDAO.getFromRs(rs, "fd.", true));
            } else if (Parameter.TYPE_PHONE.equals(type)) {
                ParameterPhoneValue value = (ParameterPhoneValue)param.getValue();
                if (value == null)
                    param.setValue(value = new ParameterPhoneValue());
                value.addItem(getParamPhoneValueItemFromRs(rs));
            } else {
                if ("encrypted".equals(param.getParameter().getConfigMap().get("encrypt")) && !offEncription) {
                    param.setValue("<ЗНАЧЕНИЕ ЗАШИФРОВАНО>");
                } else {
                    param.setValue(rs.getString(2));
                }
            }
        }
        ps.close();
    }

    public static final String PARAM_ADDRESS_FIELD_QUARTER = "quarter";
    public static final String PARAM_ADDRESS_FIELD_STREET = "street";
    public static final Set<String> PARAM_ADDRESS_FIELDS = new HashSet<String>(
            Arrays.asList(PARAM_ADDRESS_FIELD_QUARTER, PARAM_ADDRESS_FIELD_STREET));

    /**
     * Добавляет в запрос выборку параметра.
     * @param paramRef
     * @param selectPart
     * @param joinPart
     * @param addColumnValueAlias - добавляет в запрос алиас колонки, например в запросе param_79.value AS param_79_value добавит строку " AS param_79_value"
     */
    public static void paramSelectQuery(String paramRef, String linkColumn, StringBuilder selectPart,
            StringBuilder joinPart, boolean addColumnValueAlias) {
        String[] tokens = paramRef.split(":");
        if (tokens.length >= 2) {
            int paramId = Utils.parseInt(tokens[1].trim());
            String afterParamId = null;
            if (tokens.length > 2) {
                afterParamId = tokens[2].trim();
            }

            Parameter param = ParameterCache.getParameter(paramId);
            if (param == null) {
                selectPart.append("'НЕ НАЙДЕН ПАРАМЕТР:" + paramId + "' ");
            } else {
                boolean isMultiple = param.getConfigMap().getBoolean(Parameter.PARAM_MULTIPLE_KEY, false);

                String type = param.getType();

                String tableAlias = "param_" + paramId;
                if (Utils.notBlankString(afterParamId)) {
                    tableAlias += "_" + afterParamId;
                }

                String columnValueAlias = "";
                if (addColumnValueAlias) {
                    columnValueAlias = " AS param_" + paramId + "_val";
                }

                //TODO: Для списков с одним значением достаточно делать JOIN, будет быстрее..
                if (Parameter.TYPE_LIST.equals(type)) {
                    String tableName = param.getConfigMap().get(Parameter.LIST_PARAM_USE_DIRECTORY_KEY);
                    if (Utils.notBlankString(tableName)) {
                        selectPart.append("\n(SELECT GROUP_CONCAT(CONCAT(val.title, IF(param.comment, CONCAT(' [',param.comment,']'), '')) SEPARATOR ', ') ");
                        selectPart.append("FROM " + TABLE_PARAM_LIST + " AS param LEFT JOIN " + tableName
                                + " AS val ON param.value=val.id ");
                        selectPart.append("WHERE param.id=" + linkColumn + " AND param.param_id=" + paramId
                                + " GROUP BY param.id");
                        selectPart.append(") " + columnValueAlias + " ");
                    } else {
                        selectPart.append("\n(SELECT GROUP_CONCAT(CONCAT(val.title, IF(param.comment != '', CONCAT(' [',param.comment,']'), '')) SEPARATOR ', ') ");
                        selectPart.append("FROM " + TABLE_PARAM_LIST + " AS param LEFT JOIN " + TABLE_PARAM_LIST_VALUE
                                + " AS val ON param.param_id=val.param_id AND param.value=val.id ");
                        selectPart.append("WHERE param.id=" + linkColumn + " AND param.param_id=" + paramId
                                + " GROUP BY param.id");
                        selectPart.append(") " + columnValueAlias + " ");
                    }
                } else if (Parameter.TYPE_LISTCOUNT.equals(type)) {
                    String tableName = param.getConfigMap().get(Parameter.LIST_PARAM_USE_DIRECTORY_KEY);
                    if (Utils.notBlankString(tableName)) {
                        selectPart.append("\n( SELECT GROUP_CONCAT(val.title,val.count SEPARATOR ', ') ");
                        selectPart.append("FROM " + TABLE_PARAM_LISTCOUNT + " AS param LEFT JOIN " + tableName
                                + " AS val ON param.value=val.id ");
                        selectPart.append("WHERE param.id=" + linkColumn + " AND param.param_id=" + paramId
                                + " GROUP BY param.id");
                        selectPart.append(") " + columnValueAlias + " ");
                    } else {
                        selectPart.append(
                                "\n( SELECT GROUP_CONCAT( CONCAT(val.title,':',CAST(param.count AS CHAR)) SEPARATOR ', ') ");
                        selectPart.append(
                                "FROM " + TABLE_PARAM_LISTCOUNT + " AS param LEFT JOIN " + TABLE_PARAM_LISTCOUNT_VALUE
                                        + " AS val ON param.param_id=val.param_id AND param.value=val.id ");
                        selectPart.append("WHERE param.id=" + linkColumn + " AND param.param_id=" + paramId
                                + " GROUP BY param.id");
                        selectPart.append(") " + columnValueAlias + " ");
                    }
                } else if (Parameter.TYPE_TREE.equals(type)) {
                    selectPart.append("\n( SELECT GROUP_CONCAT(val.title SEPARATOR ', ') ");
                    selectPart.append("FROM " + TABLE_PARAM_TREE + " AS param LEFT JOIN " + TABLE_PARAM_TREE_VALUE
                            + " AS val ON param.param_id=val.param_id AND param.value=val.id ");
                    selectPart.append(
                            "WHERE param.id=" + linkColumn + " AND param.param_id=" + paramId + " GROUP BY param.id");
                    selectPart.append(") " + columnValueAlias + " ");
                } else if (Parameter.TYPE_ADDRESS.equals(type)) {
                    if (Utils.notBlankString(afterParamId) && !PARAM_ADDRESS_FIELDS.contains(afterParamId)) {
                        selectPart.append(
                                "\n( SELECT GROUP_CONCAT( CONCAT( CAST( house_id AS CHAR ), ':', flat, ':', room, ':', CAST( pod AS CHAR ), ':', CAST( floor AS CHAR ), ':', comment ) SEPARATOR '|') ");
                        selectPart.append("FROM " + TABLE_PARAM_ADDRESS + " AS param ");
                        selectPart.append("WHERE param.id=" + linkColumn + " AND param.param_id=" + paramId
                                + " GROUP BY param.id");
                        selectPart.append(") " + columnValueAlias + " ");
                    } else {
                        if (isMultiple) {
                            selectPart.append("\n( SELECT GROUP_CONCAT(param.value SEPARATOR '; ') ");
                            selectPart.append("FROM " + TABLE_PARAM_ADDRESS + " AS param ");
                            selectPart.append("WHERE param.id=" + linkColumn + " AND param.param_id=" + paramId
                                    + " GROUP BY param.id");
                            selectPart.append(") " + columnValueAlias + " ");
                        } else {
                            if (PARAM_ADDRESS_FIELDS.contains(afterParamId)) {
                                String joinQuery = " LEFT JOIN param_" + type + " AS " + tableAlias + " ON "
                                        + tableAlias + ".id=" + linkColumn + " AND " + tableAlias + ".param_id="
                                        + paramId;

                                addIfNotContains(joinPart, joinQuery);

                                String houseTableAlias = tableAlias + "_house";

                                joinQuery = " LEFT JOIN " + TABLE_ADDRESS_HOUSE + " AS " + houseTableAlias + " ON "
                                        + houseTableAlias + ".id=" + tableAlias + ".house_id";

                                addIfNotContains(joinPart, joinQuery);

                                if (PARAM_ADDRESS_FIELD_QUARTER.equals(afterParamId)) {
                                    String quarterTableAlias = tableAlias + "_quarter";

                                    joinQuery = " LEFT JOIN " + TABLE_ADDRESS_QUARTER + " AS " + quarterTableAlias
                                            + " ON " + quarterTableAlias + ".id=" + houseTableAlias + ".quarter_id";
                                    joinPart.append(joinQuery);

                                    selectPart.append(quarterTableAlias + ".title ");
                                } else if (PARAM_ADDRESS_FIELD_STREET.equals(afterParamId)) {
                                    String streetTableAlias = tableAlias + "_street ";

                                    joinQuery = " LEFT JOIN " + TABLE_ADDRESS_STREET + " AS " + streetTableAlias
                                            + " ON " + streetTableAlias + ".id=" + houseTableAlias + ".street_id";
                                    joinPart.append(joinQuery);

                                    selectPart.append(streetTableAlias + ".title ");
                                }
                            } else {
                                addParamValueJoin(linkColumn, joinPart, paramId, type, tableAlias);
                                selectPart.append(tableAlias);
                                selectPart.append(".value ");
                            }
                        }
                    }
                } else {
                    // TODO: Унифицировать код с ProcessDAO.addDateTimeParam
                    if ((Parameter.TYPE_DATE.equals(type) || Parameter.TYPE_DATETIME.equals(type))
                            && !"value".equals(afterParamId)) {
                        String format = SQLUtils.javaDateFormatToSql(param.getDateParamFormat());

                        selectPart.append("DATE_FORMAT(");
                        selectPart.append(tableAlias);
                        selectPart.append(".value, '");
                        selectPart.append(format);
                        selectPart.append("') " + columnValueAlias + " ");
                    } else {
                        selectPart.append(tableAlias);
                        selectPart.append(".value " + columnValueAlias + " ");
                    }

                    addParamValueJoin(linkColumn, joinPart, paramId, type, tableAlias);
                }
            }
        }
    }

    private static void addParamValueJoin(String linkColumn, StringBuilder joinPart, int paramId, String type,
            String tableAlias) {
        joinPart.append(" LEFT JOIN param_" + type);
        joinPart.append(" AS ");
        joinPart.append(tableAlias);
        joinPart.append(" ON ");
        joinPart.append(tableAlias);
        joinPart.append(".id=");
        joinPart.append(linkColumn);
        joinPart.append(" AND ");
        joinPart.append(tableAlias);
        joinPart.append(".param_id=");
        joinPart.append(paramId);
    }

    private static void addIfNotContains(StringBuilder joinPart, String joinQuery) {
        if (joinPart.indexOf(joinQuery) < 0) {
            joinPart.append(joinQuery);
        }
    }

    private void updateSimpleParam(int id, int paramId, Object value, String tableName, String tableLogName)
            throws SQLException {
        if (value == null) {
            deleteFromParamTable(id, paramId, tableName);
        } else {
            StringBuilder query = new StringBuilder(200);

            query.append(SQL_UPDATE);
            query.append(tableName);
            query.append("SET value=?");
            query.append(SQL_WHERE);
            query.append("id=? AND param_id=?");

            PreparedStatement ps = con.prepareStatement(query.toString());
            ps.setObject(1, value);
            ps.setInt(2, id);
            ps.setInt(3, paramId);

            if (ps.executeUpdate() == 0) {
                ps.close();

                query.setLength(0);
                query.append(SQL_INSERT);
                query.append(tableName);
                query.append("(id, param_id, value) VALUES (?,?,?)");

                ps = con.prepareStatement(query.toString());
                ps.setInt(1, id);
                ps.setInt(2, paramId);
                ps.setObject(3, value);
                ps.executeUpdate();
            }
            ps.close();
        }
    }

    private void deleteFromParamTable(int id, int paramId, String tableName) throws SQLException {
        String query = "DELETE FROM " + tableName + " WHERE id=? AND param_id=?";

        var ps = con.prepareStatement(query);
        ps.setInt(1, id);
        ps.setInt(2, paramId);
        ps.executeUpdate();

        ps.close();
    }

    private void addListParamJoin(StringBuilder query, int paramId) throws SQLException {
        Parameter param = ParameterCache.getParameter(paramId);
        String joinTable = param.getConfigMap().get(LIST_PARAM_USE_DIRECTORY_KEY, TABLE_PARAM_LIST_VALUE);
        addListTableJoin(query, joinTable);
    }

    private void addListCountParamJoin(StringBuilder query, int paramId) throws SQLException {
        Parameter param = ParameterCache.getParameter(paramId);
        String joinTable = param.getConfigMap().get(LIST_PARAM_USE_DIRECTORY_KEY, TABLE_PARAM_LISTCOUNT_VALUE);
        addListCountTableJoin(query, joinTable);
    }

    private void addListTableJoin(StringBuilder query, String tableName) {
        query.append(SQL_LEFT_JOIN);
        query.append(tableName);
        query.append(" AS dir ON ");
        if (tableName.equals(TABLE_PARAM_LIST_VALUE)) {
            query.append(" val.param_id=dir.param_id AND val.value=dir.id ");
        } else {
            query.append(" val.value=dir.id ");
        }
    }

    private void addListCountTableJoin(StringBuilder query, String tableName) {
        query.append(SQL_LEFT_JOIN);
        query.append(tableName);
        query.append(" AS dir ON ");
        if (tableName.equals(TABLE_PARAM_LISTCOUNT_VALUE)) {
            query.append(" val.param_id=dir.param_id AND val.value=dir.id ");
        } else {
            query.append(" val.value=dir.id ");
        }
    }

    public static ParameterAddressValue getParameterAddressValueFromRs(ResultSet rs) throws SQLException {
        return getParameterAddressValueFromRs(rs, "");
    }

    public static ParameterAddressValue getParameterAddressValueFromRs(ResultSet rs, String prefix) throws SQLException {
        return getParameterAddressValueFromRs(rs, "", false, null);
    }

    public static ParameterAddressValue getParameterAddressValueFromRs(ResultSet rs, String prefix, boolean loadDirs,
            String formatName) throws SQLException {
        ParameterAddressValue result = new ParameterAddressValue();

        result.setHouseId(rs.getInt(prefix + "house_id"));
        result.setFlat(rs.getString(prefix + "flat"));
        result.setRoom(rs.getString(prefix + "room"));
        result.setPod(rs.getInt(prefix + "pod"));
        result.setFloor(rs.getInt(prefix + "floor"));
        result.setValue(rs.getString(prefix + "value"));
        result.setComment(rs.getString(prefix + "comment"));
        result.setCustom(rs.getString(prefix + "custom"));

        if (loadDirs) {
            result.setHouse(AddressDAO.getAddressHouseFromRs(rs, "house.", LOAD_LEVEL_COUNTRY));
            if (Utils.notBlankString(formatName)) {
                result.setValue(AddressUtils.buildAddressValue(result, null, formatName));
            }
        }

        return result;
    }

    /**
     * Фильтр сущности по значению параметра, пока поддерживается только значение вида
     * param:<code>:cityId in 1,2,3
     * param:<code>:value in 1,2,3
     * @param objectId
     * @param valuesCache
     * @param equation
     * @return
     */
    //TODO: Стоит эту функцию переделать на использование getJoinFilters.
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public boolean paramValueFilter(String expression, int objectId, Map valuesCache) throws SQLException {
        String[] tokens = expression.split("\\s+");
        if (tokens.length != 3) {
            log.error("Incorrect filter expression: " + expression);
            return false;
        }

        String paramMacro = tokens[0];
        String function = tokens[1];
        Set<Integer> values = Utils.toIntegerSet(tokens[2]);

        // пока единственная разрешённая функция
        if (!function.equals("in")) {
            log.error("Incorrect function: " + function + " in expression: " + expression);
            return false;
        }

        tokens = paramMacro.split(":");
        if (!paramMacro.startsWith("param:") || tokens.length != 3) {
            log.error("Incorrect param macro: " + paramMacro + " in expression: " + expression);
            return false;
        }

        String paramId = tokens[1];
        String paramExtractor = tokens[2];

        Parameter param = ParameterCache.getParameter(Utils.parseInt(paramId));
        if (param == null) {
            log.error("Param not found: " + paramId + " or not address in expression: " + expression);
            return false;
        }

        Set<Integer> codes = new HashSet<Integer>();
        if (param.getType().equals(Parameter.TYPE_ADDRESS)) {
            Collection<ParameterAddressValue> value = (Collection<ParameterAddressValue>) valuesCache.get(paramId);
            if (value == null) {
                value = getParamAddressExt(objectId, param.getId(), true, null).values();
                valuesCache.put(paramId, value);
            }

            // текущие коды городов или чего-то там ещё в перспективе
            for (ParameterAddressValue p : value) {
                if (paramExtractor.equals("cityId")) {
                    codes.add(p.getHouse().getAddressStreet().getCityId());
                }
            }
        } else if (param.getType().equals(Parameter.TYPE_LIST)) {
            Set<Integer> value = (Set<Integer>) valuesCache.get(paramId);
            if (value == null) {
                value = getParamList(objectId, param.getId());
                valuesCache.put(paramId, value);
            }

            // функция-экстрактор фактически не обрабатывается

            codes.addAll(value);
        }

        if ("in".equals(function)) {
            return CollectionUtils.intersection(codes, values).size() > 0;
        }

        return false;
    }

    /**
     * Добавление INNER JOIN фильтров по параметрам, пока поддерживается только значение вида для списковых.
     * param:<code>:value in 1,2,3
     * @param objectId
     * @param valuesCache
     * @param equation
     * @return
     */
    public static String getParamJoinFilters(String expression, String objectId) throws SQLException {
        StringBuilder result = new StringBuilder();

        String[] tokens = expression.split("\\s+");
        if (tokens.length != 3) {
            log.error("Incorrect filter expression: " + expression);
            return "";
        }

        String paramMacro = tokens[0];
        String function = tokens[1];
        Set<Integer> values = Utils.toIntegerSet(tokens[2]);

        // пока единственная разрешённая функция
        if (!function.equals("in")) {
            log.error("Incorrect function: " + function + " in expression: " + expression);
            return "";
        }

        tokens = paramMacro.split(":");
        if (!paramMacro.startsWith("param:") || tokens.length != 3) {
            log.error("Incorrect param macro: " + paramMacro + " in expression: " + expression);
            return "";
        }

        String paramId = tokens[1];

        Parameter param = ParameterCache.getParameter(Utils.parseInt(paramId));
        if (param == null) {
            log.error("Param not found: " + paramId + " or not address in expression: " + expression);
            return "";
        }

        if ("in".equals(function)) {
            if (Parameter.TYPE_LIST.equals(param.getType())) {
                String tableName = "param_" + param.getId() + "_val";

                result.append(SQL_INNER_JOIN);
                result.append(TABLE_PARAM_LIST);
                result.append("AS " + tableName);
                result.append(" ON " + tableName + ".id=" + objectId + " AND " + tableName + ".param_id="
                        + param.getId() + " AND " + tableName + ".value IN (" + Utils.toString(values) + ") ");
            }
        }

        return result.toString();
    }

    /**
     * Изменение значения параметра типа 'tree' объекта.
     * @param id код объекта.
     * @param paramId код параметра.
     * @param values значения.
     * @throws SQLException
     */
    public void updateParamTree(int id, int paramId, Set<String> values) throws SQLException {
        List<IdStringTitle> existIdTitles = getParamTreeWithTitles(id, paramId);

        PreparedStatement ps = null;

        if (existIdTitles.size() > 0) {
            deleteFromParamTable(id, paramId, TABLE_PARAM_TREE);
        }

        String query = "INSERT INTO " + TABLE_PARAM_TREE + "(id, param_id, value) VALUES (?,?,?)";

        ps = con.prepareStatement(query.toString());
        ps.setInt(1, id);
        ps.setInt(2, paramId);
        for (String value : values) {
            ps.setString(3, value);
            ps.executeUpdate();
        }
        ps.close();

        // Лог изменений.
        if (history) {
            logParam(id, paramId, userId, Utils.getObjectTitles(getParamTreeWithTitles(id, paramId)));
        }
    }

    /**
     * Значения параметра объекта типа 'tree' с текстовыми наименованиями. 
     * @param id код объекта.
     * @param paramId код параметра.
     * @return
     * @throws SQLException
     */
    public List<IdStringTitle> getParamTreeWithTitles(int id, int paramId) throws SQLException {
        List<IdStringTitle> result = new ArrayList<>();

        StringBuilder query = new StringBuilder();

        query.append(SQL_SELECT);
        query.append("val.value, dir.title");
        query.append(SQL_FROM);
        query.append(TABLE_PARAM_TREE);
        query.append("AS val");
        addTreeParamJoin(query, paramId);
        query.append(SQL_WHERE);
        query.append("val.id=? AND val.param_id=?");

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

    private void addTreeParamJoin(StringBuilder query, int paramId) throws SQLException {
        Parameter param = ParameterCache.getParameter(paramId);
        String joinTable = param.getConfigMap().get(LIST_PARAM_USE_DIRECTORY_KEY, TABLE_PARAM_TREE_VALUE);
        addTreeTableJoin(query, joinTable);
    }

    private void addTreeTableJoin(StringBuilder query, String tableName) {
        query.append(SQL_LEFT_JOIN);
        query.append(tableName);
        query.append(" AS dir ON ");
        if (tableName.equals(TABLE_PARAM_TREE_VALUE)) {
            query.append(" val.param_id=dir.param_id AND val.value=dir.id ");
        } else {
            query.append(" val.value=dir.id ");
        }
    }

    /**
     * Поиск объектов по значениям связанного телефонного параметра
     * @param parameterId - ID параметра
     * @param parameterPhoneValue - набор телефонов для поиска
     * @return
     * @throws SQLException
     */
    public Set<Integer> searchObjectByParameterPhone(int parameterId, ParameterPhoneValue parameterPhoneValue)
            throws SQLException {
        Set<Integer> result = new HashSet<Integer>();

        StringBuilder query = new StringBuilder();
        query.append(SQL_SELECT);
        query.append("item.id AS object_id");
        query.append(SQL_FROM);
        query.append(TABLE_PARAM_PHONE_ITEM);
        query.append(" AS item ");
        query.append(SQL_WHERE);
        query.append("item.param_id = ? ");
        query.append("AND (");

        List<ParameterPhoneValueItem> phoneItems = parameterPhoneValue.getItemList();

        for (int index = 0; index < phoneItems.size(); index++) {
            if (index > 0) {
                query.append(" OR");
            }

            query.append(" item.phone LIKE '%" + phoneItems.get(index).getPhone() + "'");
        }

        query.append(" )");

        PreparedStatement ps = con.prepareStatement(query.toString());
        ps.setInt(1, parameterId);

        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            result.add(rs.getInt("object_id"));
        }

        ps.close();

        return result;
    }

    /**
     * Функция поиска объектов по значениям связанного адресного параметра
     * @param parameterId - ID параметра
     * @param parameterAddressValue - значение адресного параметра
     * @return
     * @throws SQLException
     */
    public Set<Integer> searchObjectByParameterAddress(int parameterId, ParameterAddressValue parameterAddressValue)
            throws SQLException {
        Set<Integer> result = new HashSet<Integer>();

        StringBuilder query = new StringBuilder();
        query.append(SQL_SELECT);
        query.append("address.id AS object_id ");
        query.append(SQL_FROM);
        query.append(TABLE_PARAM_ADDRESS);
        query.append(" AS address ");
        query.append(SQL_WHERE);
        query.append("address.param_id = ? ");
        query.append("AND address.house_id = ? ");
        query.append("AND address.flat = ? ");
        query.append("AND address.room = ? ");
        query.append("AND address.pod = ? ");
        query.append("AND address.floor = ? ");

        PreparedStatement ps = con.prepareStatement(query.toString());

        ps.setInt(1, parameterId);
        ps.setInt(2, parameterAddressValue.getHouseId());
        ps.setString(3, parameterAddressValue.getFlat());
        ps.setString(4, parameterAddressValue.getRoom());
        ps.setInt(5, parameterAddressValue.getPod());
        ps.setInt(6, parameterAddressValue.getFloor());

        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            result.add(rs.getInt("object_id"));
        }

        ps.close();

        return result;
    }

    /**
     * Поиск объектов по значнию текстового параметра.
     * @param parameterId код параметра.
     * @param parameterTextValue точное значение.
     * @return список с кодами объектов.
     * @throws SQLException
     */
    public Set<Integer> searchObjectByParameterText(int parameterId, String parameterTextValue) throws SQLException {
        Set<Integer> result = new HashSet<Integer>();

        StringBuilder query = new StringBuilder();
        query.append(SQL_SELECT);
        query.append("text.id AS object_id ");
        query.append(SQL_FROM);
        query.append(TABLE_PARAM_TEXT);
        query.append(" AS text ");
        query.append(SQL_WHERE);
        query.append("text.param_id = ? ");
        query.append("AND text.value = ? ");

        PreparedStatement ps = con.prepareStatement(query.toString());

        ps.setInt(1, parameterId);
        ps.setString(2, parameterTextValue);

        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            result.add(rs.getInt("object_id"));
        }

        ps.close();

        return result;
    }

    /**
     * Searches object IDs by list parameter value.
     * @param parameterId
     * @param value
     * @return
     * @throws Exception
     */
    public Set<Integer> searchObjectByParameterList(int parameterId, int value) throws Exception {
        Set<Integer> result = new HashSet<>();

        try (var pd = new PreparedDelay(con)) {
            pd.addQuery(SQL_SELECT);
            pd.addQuery("list.id AS object_id");
            pd.addQuery(SQL_FROM);
            pd.addQuery(TABLE_PARAM_LIST);
            pd.addQuery(" AS list ");
            pd.addQuery(SQL_WHERE);
            pd.addQuery("list.param_id=? AND list.value=?");

            pd.addInt(parameterId);
            pd.addInt(value);

            try (var rs = pd.executeQuery()) {
                while (rs.next())
                    result.add(rs.getInt(1));
            }
        }

        return result;
    }
    
    public static ParameterPhoneValueItem getParamPhoneValueItemFromRs(ResultSet rs) throws SQLException { 
        ParameterPhoneValueItem item = new ParameterPhoneValueItem();
        item.setPhone(rs.getString("phone"));
        item.setFormat(rs.getString("format"));
        item.setComment(rs.getString("comment"));
        item.setFlags(rs.getInt("flags"));
        return item;
    }

}
