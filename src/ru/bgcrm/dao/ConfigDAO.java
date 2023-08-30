package ru.bgcrm.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.collections.CollectionUtils;
import org.bgerp.model.Pageable;

import ru.bgcrm.model.Config;
import ru.bgcrm.model.ConfigRecord;
import ru.bgcrm.model.Page;
import ru.bgcrm.util.Utils;

public class ConfigDAO extends CommonDAO {
    public static final String TABLE_CONFIG_GLOBAL = " config_global ";
    @Deprecated
    private String tableName;

    public ConfigDAO(Connection con) {
        super(con);
    }

    /**
     * Used only in address editor, clean up later.
     * @param con
     * @param tableName
     */
    public ConfigDAO(Connection con, String tableName) {
        super(con);
        this.tableName = tableName;
    }

    public void searchGlobalConfigList(Pageable<Config> searchResult, Set<Integer> allowedConfigIds, String filterLike)
            throws SQLException {
        if (searchResult != null) {
            Page page = searchResult.getPage();
            List<Config> list = searchResult.getList();

            StringBuilder query = new StringBuilder();
            query.append(SQL_SELECT_COUNT_ROWS);
            query.append("*");
            query.append(SQL_FROM);
            query.append(TABLE_CONFIG_GLOBAL);

            query.append(SQL_WHERE);
            query.append("1>0");

            if (CollectionUtils.isNotEmpty(allowedConfigIds)) {
                query.append(SQL_AND);
                query.append("id IN (");
                query.append(Utils.toString(allowedConfigIds));
                query.append(")");
            }
            if (Utils.notBlankString(filterLike)) {
                query.append(SQL_AND);
                query.append("data LIKE '");
                query.append(filterLike);
                query.append("' ");
            }
            query.append(SQL_ORDER_BY);
            query.append("title");
            query.append(getPageLimit(page));

            PreparedStatement ps = con.prepareStatement(query.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(getGlobalConfigFromRs(rs));
            }

            page.setRecordCount(foundRows(ps));
            ps.close();
        }
    }

    public Config getGlobalConfig(int id) throws SQLException {
        Config config = null;

        int index = 1;

        StringBuilder query = new StringBuilder();
        query.append(SQL_SELECT);
        query.append("*");
        query.append(SQL_FROM);
        query.append(TABLE_CONFIG_GLOBAL);
        query.append(SQL_WHERE);
        query.append("id=?");

        PreparedStatement ps = con.prepareStatement(query.toString());
        ps.setInt(index++, id);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            config = getGlobalConfigFromRs(rs);
            LastModifyDAO.setLastModify(config, rs);
        }
        ps.close();

        return config;
    }

    public Config getActiveGlobalConfig() throws SQLException {
        Config config = null;

        StringBuilder query = new StringBuilder();
        query.append(SQL_SELECT);
        query.append("*");
        query.append(SQL_FROM);
        query.append(TABLE_CONFIG_GLOBAL);
        query.append(SQL_WHERE);
        query.append("active=true");

        PreparedStatement ps = con.prepareStatement(query.toString());
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            config = getGlobalConfigFromRs(rs);
        }
        ps.close();

        return config;
    }

    public void setActiveGlobalConfig(int id) throws SQLException {
        int index = 1;
        boolean needUpdate = false;
        StringBuilder query = new StringBuilder();
        query.append(SQL_SELECT);
        query.append("active");
        query.append(SQL_FROM);
        query.append(TABLE_CONFIG_GLOBAL);
        query.append(SQL_WHERE);
        query.append("id=?");
        PreparedStatement ps = con.prepareStatement(query.toString());
        ps.setInt(index++, id);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            needUpdate = !rs.getBoolean(1);
        }
        ps.close();
        if (needUpdate) {
            query = new StringBuilder();
            query.append(SQL_UPDATE);
            query.append(TABLE_CONFIG_GLOBAL);
            query.append(SQL_SET);
            query.append("active=false");
            ps = con.prepareStatement(query.toString());
            ps.executeUpdate();
            ps.close();

            index = 1;
            query = new StringBuilder();
            query.append(SQL_UPDATE);
            query.append(TABLE_CONFIG_GLOBAL);
            query.append(SQL_SET);
            query.append("active=true");
            query.append(SQL_WHERE);
            query.append("id=?");
            ps = con.prepareStatement(query.toString());
            ps.setInt(index++, id);
            ps.executeUpdate();
            ps.close();
        }
    }

    public void updateGlobalConfig(Config config) throws SQLException {
        if (config == null) {
            return;
        }

        int index = 1;

        if (config.getId() <= 0) {
            StringBuilder query = new StringBuilder();
            query.append(SQL_INSERT);
            query.append(TABLE_CONFIG_GLOBAL);
            query.append(SQL_SET);
            query.append("title=?, data=?, parent_id=?, " + LastModifyDAO.LAST_MODIFY_COLUMNS);
            PreparedStatement ps = con.prepareStatement(query.toString(), PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(index++, config.getTitle());
            ps.setString(index++, config.getData());
            ps.setInt(index++, config.getParentId());
            LastModifyDAO.setLastModifyFields(ps, index++, index++, config.getLastModify());
            ps.executeUpdate();
            config.setId(lastInsertId(ps));
            ps.close();
        } else {
            StringBuilder query = new StringBuilder();
            query.append(SQL_UPDATE);
            query.append(TABLE_CONFIG_GLOBAL);
            query.append(SQL_SET);
            query.append("title=?, data=?, parent_id=?, " + LastModifyDAO.LAST_MODIFY_COLUMNS);
            query.append(SQL_WHERE);
            query.append("id=?");
            PreparedStatement ps = con.prepareStatement(query.toString());
            ps.setString(index++, config.getTitle());
            ps.setString(index++, config.getData());
            ps.setInt(index++, config.getParentId());
            LastModifyDAO.setLastModifyFields(ps, index++, index++, config.getLastModify());
            ps.setInt(index++, config.getId());
            ps.executeUpdate();
            ps.close();
        }
    }

    /**
     * Selects included configurations.
     * @param parentId
     * @return map with key {@link Config#getId()}
     * @throws SQLException
     */
    public Map<Integer, String> getIncludes(int parentId) throws SQLException {
        var result = new TreeMap<Integer, String>();

        String query = SQL_SELECT_ALL_FROM + TABLE_CONFIG_GLOBAL + SQL_WHERE + "parent_id=?";
        var ps = con.prepareStatement(query);
        ps.setInt(1, parentId);

        try (var rs = ps.executeQuery()) {
            while (rs.next()) {
                var config = getGlobalConfigFromRs(rs);
                result.put(config.getId(), config.getData());
            }
        }

        return result;
    }

    public void deleteGlobalConfig(int id) throws SQLException {
        if (id > 0) {
            int index = 1;
            StringBuilder query = new StringBuilder();
            query.append(SQL_DELETE);
            query.append(TABLE_CONFIG_GLOBAL);
            query.append(SQL_WHERE);
            query.append("active=false AND id=?");
            PreparedStatement ps = con.prepareStatement(query.toString());
            ps.setInt(index++, id);
            ps.executeUpdate();
            ps.close();
        }
    }

    @Deprecated
    public List<ConfigRecord> getConfigRecordList(String tableId, int recordId) throws SQLException {
        List<ConfigRecord> result = new ArrayList<ConfigRecord>();

        PreparedStatement ps = con
                .prepareStatement("SELECT * FROM " + tableName + " WHERE table_id=? AND record_id=? ORDER BY `key`");
        ps.setString(1, tableId);
        ps.setInt(2, recordId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            ConfigRecord configRecord = new ConfigRecord();
            setConfigRecordData(configRecord, rs);
            result.add(configRecord);
        }
        ps.close();

        return result;
    }

    @Deprecated
    public Map<String, String> getConfigRecordMap(String tableId, int recordId) throws SQLException {
        // таблица может быть передана из констант, тогда она окружена пробелами
        tableId = tableId.trim();

        Map<String, String> configMap = new HashMap<String, String>();
        for (ConfigRecord configRecord : getConfigRecordList(tableId, recordId)) {
            configMap.put(configRecord.getKey(), configRecord.getValue());
        }
        return configMap;
    }

    @Deprecated
    public void updateConfigForRecord(String tableId, int recordId, Map<String, String> config) throws SQLException {
        // таблица может быть передана из констант, тогда она окружена пробелами
        tableId = tableId.trim();

        if (config != null) {
            List<ConfigRecord> configList = new ArrayList<ConfigRecord>();
            for (String key : config.keySet()) {
                ConfigRecord configRecord = new ConfigRecord();
                configRecord.setKey(key);
                configRecord.setValue(config.get(key));
                configList.add(configRecord);
            }
            updateConfigForRecord(tableId, recordId, configList);
        }
    }

    @Deprecated
    public void updateConfigForRecord(String tableId, int recordId, List<ConfigRecord> config) throws SQLException {
        // таблица может быть передана из констант, тогда она окружена пробелами
        tableId = tableId.trim();

        PreparedStatement ps = null;

        ps = con.prepareStatement("DELETE FROM " + tableName + " WHERE table_id=? AND record_id=?");
        ps.setString(1, tableId);
        ps.setInt(2, recordId);
        ps.executeUpdate();
        ps.close();

        ps = con.prepareStatement("INSERT INTO " + tableName + " SET table_id=?, record_id=?, `key`=?, value=?");
        ps.setString(1, tableId);
        ps.setInt(2, recordId);
        for (ConfigRecord configRecord : config) {
            ps.setString(3, configRecord.getKey());
            ps.setString(4, configRecord.getValue());
            ps.executeUpdate();
        }
        ps.close();
    }

    private Config getGlobalConfigFromRs(ResultSet rs) throws SQLException {
        Config config = new Config();
        config.setId(rs.getInt("id"));
        config.setParentId(rs.getInt("parent_id"));
        config.setTitle(rs.getString("title"));
        config.setData(rs.getString("data"));
        config.setActive(rs.getBoolean("active"));
        return config;
    }

    private void setConfigRecordData(ConfigRecord configRecord, ResultSet rs) throws SQLException {
        configRecord.setTableId(rs.getString("table_id"));
        configRecord.setRecordId(rs.getInt("record_id"));
        configRecord.setKey(rs.getString("key"));
        configRecord.setValue(rs.getString("value"));
    }
}