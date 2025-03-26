package ru.bgcrm.dao.process;

import static ru.bgcrm.dao.process.Tables.TABLE_PROCESS_STATUS_TITLE;
import static ru.bgcrm.dao.process.Tables.TABLE_PROCESS_TYPE;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bgerp.app.exception.BGException;
import org.bgerp.cache.ProcessTypeCache;
import org.bgerp.model.Pageable;
import org.bgerp.util.sql.PreparedQuery;

import ru.bgcrm.dao.CommonDAO;
import ru.bgcrm.dao.LastModifyDAO;
import ru.bgcrm.model.Page;
import ru.bgcrm.model.process.ProcessType;
import ru.bgcrm.model.process.Status;
import ru.bgcrm.model.process.TypeProperties;
import ru.bgcrm.util.Utils;

public class ProcessTypeDAO extends CommonDAO {
    private static final String SHORT_COLUMN_LIST = "id, title, parent_id, child_count, use_parent_props";

    public ProcessTypeDAO(Connection con) {
        super(con);
    }

    /**
     * Searches process types
     * @param result the result
     * @param parentId when greater 0 - filter by parent type id
     * @param filterLike when not {@code null} or empty, SQL LIKE expression to filter by ID, title, or config
     * @throws Exception
     */
    public void searchProcessType(Pageable<ProcessType> result, int parentId, String filterLike) throws Exception {
        if (result != null) {
            Page page = result.getPage();
            List<ProcessType> list = result.getList();

            try (var pq = new PreparedQuery(con)) {
                pq.addQuery(SQL_SELECT_COUNT_ROWS);
                pq.addQuery(SHORT_COLUMN_LIST);
                pq.addQuery(SQL_FROM);
                pq.addQuery(TABLE_PROCESS_TYPE);
                pq.addQuery(SQL_WHERE);
                pq.addQuery("1>0");

                if (parentId >= 0) {
                    pq.addQuery(" AND parent_id=?");
                    pq.addInt(parentId);
                }
                if (Utils.notBlankString(filterLike)) {
                    pq.addQuery(" AND (id LIKE ? OR title LIKE ? OR config LIKE ?)");
                    pq.addString(filterLike);
                    pq.addString(filterLike);
                    pq.addString(filterLike);
                }

                pq.addQuery(SQL_ORDER_BY);
                pq.addQuery("title");
                pq.addQuery(page.getLimitSql());

                ResultSet rs = pq.executeQuery();
                while (rs.next())
                    list.add(getTypeFromRs(rs, false));

                page.setRecordCount(pq.getPrepared());
            }
        }
    }

    /**
     * Выбирает тип процесса по коду.
     * @param id
     * @return
     * @throws Exception
     */
    public ProcessType getProcessType(int id) throws Exception {
        ProcessType result = null;

        ResultSet rs = null;
        PreparedStatement ps = null;
        ps = con.prepareStatement("SELECT * FROM " + TABLE_PROCESS_TYPE + " WHERE id=?");
        ps.setInt(1, id);
        rs = ps.executeQuery();
        while (rs.next()) {
            result = getTypeFromRs(rs, true);
        }
        ps.close();

        if (result != null && result.getId() > 0) {
            ps = con.prepareStatement("SELECT * FROM " + TABLE_PROCESS_TYPE + " WHERE parent_id=?");
            ps.setInt(1, result.getId());
            rs = ps.executeQuery();
            while (rs.next()) {
                result.addChild(getTypeFromRs(rs, true));
            }
            ps.close();
        }

        return result;
    }

    public List<ProcessType> getTypeChildren(int parentId, Set<Integer> excludeIds) throws Exception {
        var result = new ArrayList<ProcessType>();

        var query = "SELECT * FROM " + TABLE_PROCESS_TYPE + SQL_WHERE + "parent_id=?";
        if (excludeIds != null)
            query += SQL_AND + "id NOT IN (" + Utils.toString(excludeIds) + ")";
        query += SQL_ORDER_BY + "title";
        try (var ps = con.prepareStatement(query)) {
            ps.setInt(1, parentId);

            var rs = ps.executeQuery();
            while (rs.next())
                result.add(getTypeFromRs(rs, false));
        }

        return result;
    }

    /**
     * Возвращает список всех типов процессов с сортировкой по наименованию.
     * @return
     * @throws SQLException
     */
    public List<ProcessType> getFullProcessTypeList() throws SQLException {
        List<ProcessType> result = new ArrayList<>();

        Map<Integer, ProcessType> typeMap = new HashMap<>();

        //TODO: Может сделать сортировку по parent_id, title, тогда бы можно было за один проход загружать всё.
        try (var ps = con.prepareStatement(SQL_SELECT_ALL_FROM + TABLE_PROCESS_TYPE + SQL_ORDER_BY + "title")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ProcessType type = getTypeFromRs(rs, true);
                typeMap.put(type.getId(), type);
                result.add(type);
            }
        }

        for (ProcessType type : result) {
            if (type.isUseParentProperties()) {
                setParentTypeProperties(type, typeMap, type.getParentId());
            }
        }

        // добавляем дочерние типы процессов
        for (ProcessType type : result) {
            if (type.getParentId() > 0) {
                ProcessType parentType = typeMap.get(type.getParentId());
                parentType.addChild(type);
            }
        }

        return result;
    }

    private void setParentTypeProperties(ProcessType type, Map<Integer, ProcessType> typeMap, int parentId) {
        ProcessType parent = typeMap.get(parentId);
        if (parent != null) {
            if (parent.isUseParentProperties()) {
                setParentTypeProperties(type, typeMap, parent.getParentId());
            } else {
                type.setProperties(parent.getProperties());
            }
        }
    }

    /** Use {@link ProcessTypeCache} */
    @Deprecated
    public List<Status> getSortedProcessTypeStatusList(ProcessType type, List<Integer> sortingId) {
        List<Status> result = new ArrayList<>();

        List<Status> statusList = getProcessTypeStatusList(type);

        for (Integer id : sortingId) {
            for (Status status : statusList) {
                if (status.getId() == id) {
                    result.add(status);
                }
            }
        }

        return result;
    }

    /** Use {@link ProcessTypeCache} */
    @Deprecated
    public List<Status> getProcessTypeStatusList(ProcessType type) {
        try {
            List<Status> result = new ArrayList<>();

            ResultSet rs = null;
            PreparedStatement ps = null;
            String query = null;

            query = "SELECT status.* FROM " + TABLE_PROCESS_STATUS_TITLE + " AS status " + "WHERE status.id IN ( "
                    + Utils.toString(type.getProperties().getStatusIds(), "-1", ",") + ") " + "ORDER BY pos";
            ps = con.prepareStatement(query);
            rs = ps.executeQuery();
            while (rs.next()) {
                result.add(StatusDAO.getStatusFromRs(rs));
            }
            ps.close();

            return result;
        } catch (SQLException e) {
            throw new BGException(e);
        }
    }

    /**
     * Updates or creates a process type entity.
     * @param processType
     * @param userId
     * @throws Exception
     */
    public void updateProcessType(ProcessType processType, int userId) throws Exception {
        // when moving to a new parent
        setChildCount(processType.getId(), -1);

        int index = 1;
        PreparedStatement ps = null;

        if (processType.getId() <= 0) {
            ps = con.prepareStatement("INSERT INTO " + TABLE_PROCESS_TYPE
                    + " SET title=?, parent_id=?, use_parent_props=?, data=?, config=?, last_modify_user_id=?, last_modify_dt=NOW()",
                    PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(index++, processType.getTitle());
            ps.setInt(index++, processType.getParentId());
            ps.setBoolean(index++, processType.isUseParentProperties());
            ps.setString(index++, "");
            ps.setString(index++, "");
            ps.setInt(index++, userId);
            ps.executeUpdate();
            processType.setId(lastInsertId(ps));
        } else {
            ps = con.prepareStatement("UPDATE " + TABLE_PROCESS_TYPE + " SET title=?, parent_id=?, use_parent_props=? WHERE id=?");
            ps.setString(index++, processType.getTitle());
            ps.setInt(index++, processType.getParentId());
            ps.setBoolean(index++, processType.isUseParentProperties());
            ps.setInt(index++, processType.getId());
            ps.executeUpdate();
        }
        ps.close();

        setChildCount(processType.getId(), 0);
    }

    /**
     * Обновляет свойства типа процесса.
     * @param type
     */
    public void updateTypeProperties(ProcessType type) {
        try {
            StringBuilder query = new StringBuilder();
            query.append(SQL_UPDATE);
            query.append(TABLE_PROCESS_TYPE);
            query.append(SQL_SET);
            query.append("data=?, config=?, " + LastModifyDAO.LAST_MODIFY_COLUMNS);
            query.append(SQL_WHERE);
            query.append("id=?");

            TypeProperties properties = type.getProperties();

            PreparedStatement ps = con.prepareStatement(query.toString());
            ps.setString(1, properties.serializeToData());
            ps.setString(2, properties.getConfig());
            LastModifyDAO.setLastModifyFields(ps, 3, 4, type.getProperties().getLastModify());
            ps.setInt(5, type.getId());
            ps.executeUpdate();

            ps.close();
        } catch (SQLException e) {
            throw new BGException(e);
        }
    }

    /**
     * Copy process type properties.
     * @param fromTypeId
     * @param toTypeId
     * @throws Exception
     */
    public void copyTypeProperties(int fromTypeId, int toTypeId) throws Exception {
        var type = getProcessType(fromTypeId);
        type.setId(toTypeId);
        updateTypeProperties(type);
    }

    /**
     * true если каталог пуст и можно удалять
     * @param id
     */
    public boolean checkProcessTypeForDelete(int id) throws Exception {
        setChildCount(id, 0);
        ProcessType processType = getProcessType(id);
        if (processType != null) {
            return processType.getChildCount() == 0;
        }

        return false;
    }

    /**
     * Удаляет тип процесса.
     * @param id
     * @return
     * @throws Exception
     */
    public boolean deleteProcessType(int id) throws Exception {
        boolean result = false;
        setChildCount(id, -1);
        if (id > 0) {
            int index = 1;
            PreparedStatement ps = null;

            ps = con.prepareStatement("DELETE FROM " + TABLE_PROCESS_TYPE + " WHERE id=?");
            ps.setInt(index++, id);
            result = ps.executeUpdate() > 0;
            ps.close();
        }

        return result;
    }


    private ProcessType getTypeFromRs(ResultSet rs, boolean loadFull) throws SQLException {
        ProcessType type = new ProcessType();

        type.setId(rs.getInt("id"));
        type.setTitle(rs.getString("title"));
        type.setParentId(rs.getInt("parent_id"));
        type.setUseParentProperties(rs.getBoolean("use_parent_props"));
        if (loadFull) {
            type.setProperties(new TypeProperties(rs.getString("data"), rs.getString("config"), LastModifyDAO.getLastModify(rs)));
        }

        return type;
    }

    private void setChildCount(int chilsId, int countCorrect) {
        try {
            ResultSet rs = null;
            PreparedStatement ps = null;

            ps = con.prepareStatement("select count(*), t1.parent_id from " + TABLE_PROCESS_TYPE + " as t1 join " + TABLE_PROCESS_TYPE
                    + " as t2 where t2.id = ? AND t1.parent_id = t2.parent_id");
            ps.setInt(1, chilsId);
            rs = ps.executeQuery();
            int count = 0;
            int parentId = 0;
            if (rs.next()) {
                count = rs.getInt(1);
                parentId = rs.getInt(2);
            }
            ps.close();
            count += countCorrect;
            PreparedStatement psUpdate = con.prepareStatement("UPDATE " + TABLE_PROCESS_TYPE + " SET child_count=? WHERE id=? ");
            int index = 1;
            psUpdate.setInt(index++, count);
            psUpdate.setInt(index++, parentId);
            psUpdate.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            throw new BGException(e);
        }
    }

    /**
     * Проверяет наличие в родительском типе дочернего с указанным названием.
     * @param parentId
     * @param name
     * @return
     */
    public boolean checkType(int id, int parentId, String title) throws Exception {
        boolean result = false;

        StringBuilder query = new StringBuilder();
        query.append(SQL_SELECT);
        query.append("COUNT(*)");
        query.append(SQL_FROM);
        query.append(TABLE_PROCESS_TYPE);
        query.append(SQL_WHERE);
        query.append("id !=? AND parent_id=? AND title=?");
        PreparedStatement ps = con.prepareStatement(query.toString());
        int index = 1;
        ps.setInt(index++, id);
        ps.setInt(index++, parentId);
        ps.setString(index++, title);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            result = rs.getInt(1) == 0;
        }
        ps.close();

        return result;
    }
}