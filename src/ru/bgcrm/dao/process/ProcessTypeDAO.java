package ru.bgcrm.dao.process;

import static ru.bgcrm.dao.process.Tables.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.bgcrm.cache.ProcessTypeCache;
import ru.bgcrm.dao.CommonDAO;
import ru.bgcrm.dao.LastModifyDAO;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.Page;
import ru.bgcrm.model.SearchResult;
import ru.bgcrm.model.process.ProcessType;
import ru.bgcrm.model.process.Status;
import ru.bgcrm.model.process.TypeProperties;
import ru.bgcrm.model.process.TypeTreeItem;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.PreparedDelay;

public class ProcessTypeDAO extends CommonDAO {
    private static final String SHORT_COLUMN_LIST = "id, title, parent_id, child_count, use_parent_props, archive";

    public ProcessTypeDAO(Connection con) {
        super(con);
    }

    /**
     * Ищет типы процессов.
     * @param searchResult результат
     * @param parentId если больше либо равен 0 - фильтр по родительскому узлу
     * @param archive если не null - признак, что тип процесса помечен неиспользуемым
     * @param filterLike если не null - SQL LIKE выражение, фильтр по наименованию типа либо конфигурации
     * @throws BGException
     */
    public void searchProcessType(SearchResult<ProcessType> searchResult, int parentId, Boolean archive, String filterLike) throws BGException {
        try {
            if (searchResult != null) {
                Page page = searchResult.getPage();
                List<ProcessType> list = searchResult.getList();

                PreparedDelay ps = new PreparedDelay(con);
                ;

                ps.addQuery(SQL_SELECT_COUNT_ROWS);
                ps.addQuery(SHORT_COLUMN_LIST);
                ps.addQuery(SQL_FROM);
                ps.addQuery(TABLE_PROCESS_TYPE);
                ps.addQuery(SQL_WHERE);
                ps.addQuery("1>0");

                if (parentId >= 0) {
                    ps.addQuery(" AND parent_id=?");
                    ps.addInt(parentId);
                }
                if (Utils.notBlankString(filterLike)) {
                    ps.addQuery(" AND (title LIKE ? OR config LIKE ?)");
                    ps.addString(filterLike);
                    ps.addString(filterLike);
                }
                if (archive != null) {
                    ps.addQuery(" AND archive=?");
                    ps.addBoolean(archive);
                }

                ps.addQuery(SQL_ORDER_BY);
                ps.addQuery("title");
                ps.addQuery(getPageLimit(page));

                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    list.add(getTypeFromRs(rs, false));
                }
                page.setRecordCount(getFoundRows(ps.getPrepared()));
                ps.close();
            }
        } catch (SQLException e) {
            throw new BGException(e);
        }
    }

    /**
     * Выбирает тип процесса по коду.
     * @param id
     * @return
     * @throws BGException
     */
    public ProcessType getProcessType(int id) throws BGException {
        try {
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
        } catch (SQLException e) {
            throw new BGException(e);
        }
    }

    /**
     * Возвращает список всех типов процессов с сортировкой по наименованию.
     * @return
     * @throws BGException
     */
    public List<ProcessType> getFullProcessTypeList() throws BGException {
        try {
            List<ProcessType> result = new ArrayList<ProcessType>();

            Map<Integer, ProcessType> typeMap = new HashMap<Integer, ProcessType>();

            //TODO: Может сделать сортировку по parent_id, title, тогда бы можно было за один проход загружать всё.
            PreparedStatement ps = con.prepareStatement("SELECT * FROM " + TABLE_PROCESS_TYPE + " ORDER BY title");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ProcessType type = getTypeFromRs(rs, true);
                typeMap.put(type.getId(), type);
                result.add(type);
            }
            ps.close();

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
        } catch (SQLException e) {
            throw new BGException(e);
        }
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

    /** Использовать {@link ProcessTypeCache} */
    @Deprecated
    public List<Status> getSortedProcessTypeStatusList(ProcessType type, List<Integer> sortingId) throws BGException {
        List<Status> result = new ArrayList<Status>();

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

    /**
     * Возвращает список допустимых статусов для типа процесса.
     * @param type
     * @return
     * @throws BGException
     */
    public List<Status> getProcessTypeStatusList(ProcessType type) throws BGException {
        try {
            List<Status> result = new ArrayList<Status>();

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
     * Обновляет/добавляет тип процесса.
     * @param processType
     * @param userId
     * @throws BGException
     */
    public void updateProcessType(ProcessType processType, int userId) throws BGException {
        try {
            //это надо если мы переносим в другую ветку
            setChildCount(processType.getId(), -1);

            int index = 1;
            PreparedStatement ps = null;

            if (processType.getId() <= 0) {
                ps = con.prepareStatement("INSERT INTO " + TABLE_PROCESS_TYPE
                        + " SET title=?, archive=?, parent_id=?, use_parent_props=?, data=?, config=?, last_modify_user_id=?, last_modify_dt=NOW()",
                        PreparedStatement.RETURN_GENERATED_KEYS);
                ps.setString(index++, processType.getTitle());
                ps.setBoolean(index++, processType.isArchive());
                ps.setInt(index++, processType.getParentId());
                ps.setBoolean(index++, processType.isUseParentProperties());
                ps.setString(index++, "");
                ps.setString(index++, "");
                ps.setInt(index++, userId);
                ps.executeUpdate();
                processType.setId(lastInsertId(ps));
            } else {
                ps = con.prepareStatement("UPDATE " + TABLE_PROCESS_TYPE + " SET title=?, archive=?, parent_id=?, use_parent_props=? WHERE id=?");
                ps.setString(index++, processType.getTitle());
                ps.setBoolean(index++, processType.isArchive());
                ps.setInt(index++, processType.getParentId());
                ps.setBoolean(index++, processType.isUseParentProperties());
                ps.setInt(index++, processType.getId());
                ps.executeUpdate();
            }
            ps.close();

            setChildCount(processType.getId(), 0);
        } catch (SQLException e) {
            throw new BGException(e);
        }
    }

    /**
     * Обновляет свойства типа процесса.
     * @param type
     * @throws BGException
     */
    public void updateTypeProperties(ProcessType type) throws BGException {
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
     * true если каталог пуст и можно удалять
     * @param id
     */
    public boolean checkProcessTypeForDelete(int id) throws BGException {
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
     * @throws BGException
     */
    public boolean deleteProcessType(int id) throws BGException {
        try {
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
        } catch (SQLException e) {
            throw new BGException(e);
        }
    }

    /**
     * Выбирает дерево типов процессов из базы, возвращая его корневой элемент.
     * Возможно получение этих же данных из кэша {@link ProcessTypeCache#getTypeTreeRoot()}.
     * @return
     * @throws BGException
     */
    public TypeTreeItem getTypeTreeRoot() throws BGException {
        try {
            TypeTreeItem result = new TypeTreeItem();
            result.setId(0);

            Map<Integer, List<ProcessType>> byParentMap = new HashMap<Integer, List<ProcessType>>();

            StringBuilder query = new StringBuilder();
            query.append(SQL_SELECT);
            query.append("*");
            query.append(SQL_FROM);
            query.append(TABLE_PROCESS_TYPE);
            query.append(SQL_ORDER_BY);
            query.append("title");

            // раскладываем по предкам
            PreparedStatement ps = con.prepareStatement(query.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ProcessType type = getTypeFromRs(rs, true);

                List<ProcessType> childList = byParentMap.get(type.getParentId());
                if (childList == null) {
                    childList = new ArrayList<ProcessType>();
                    byParentMap.put(type.getParentId(), childList);
                }
                childList.add(type);
            }

            ps.close();

            addTreeItems(result, byParentMap);

            return result;
        } catch (SQLException e) {
            throw new BGException(e);
        }
    }

    private void addTreeItems(TypeTreeItem parent, Map<Integer, List<ProcessType>> byParentMap) {
        List<ProcessType> subTypeList = byParentMap.get(parent.getId());
        if (subTypeList != null) {
            for (ProcessType child : subTypeList) {
                TypeTreeItem childItem = new TypeTreeItem();
                childItem.setId(child.getId());
                childItem.setTitle(child.getTitle());
                parent.addChild(childItem);

                addTreeItems(childItem, byParentMap);
            }
        }
    }

    private ProcessType getTypeFromRs(ResultSet rs, boolean loadFull) throws SQLException {
        ProcessType type = new ProcessType();

        type.setId(rs.getInt("id"));
        type.setTitle(rs.getString("title"));
        type.setParentId(rs.getInt("parent_id"));
        type.setUseParentProperties(rs.getBoolean("use_parent_props"));
        type.setArchive(rs.getBoolean("archive"));
        if (loadFull) {
            type.setProperties(new TypeProperties(rs.getString("data"), rs.getString("config"), LastModifyDAO.getLastModify(rs)));
        }

        return type;
    }

    private void setChildCount(int chilsId, int countCorrect) throws BGException {
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
    public boolean checkType(int id, int parentId, String title) throws BGException {
        try {
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
        } catch (SQLException e) {
            throw new BGException(e);
        }
    }
}