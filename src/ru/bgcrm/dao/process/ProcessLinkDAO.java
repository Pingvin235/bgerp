package ru.bgcrm.dao.process;

import static ru.bgcrm.dao.process.Tables.TABLE_PROCESS;
import static ru.bgcrm.dao.process.Tables.TABLE_PROCESS_LINK;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;

import org.apache.commons.collections.CollectionUtils;

import ru.bgcrm.dao.CommonLinkDAO;
import ru.bgcrm.dao.ParamValueDAO;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.Page;
import ru.bgcrm.model.Pair;
import ru.bgcrm.model.SearchResult;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.user.User;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.PreparedDelay;

public class ProcessLinkDAO extends CommonLinkDAO {
    private static final Set<String> CYCLES_CONTROL_LINK_TYPES = Sets.newHashSet(Process.LINK_TYPE_DEPEND, Process.LINK_TYPE_MADE);

    private final User user;
    
    public ProcessLinkDAO(Connection con) {
        super(con);
        this.user = User.USER_SYSTEM;
    }
    
    public ProcessLinkDAO(Connection con, User user) {
        super(con);
        this.user = user;
    }

    @Override
    protected String getTable() {
        return TABLE_PROCESS_LINK;
    }

    @Override
    protected String getColumnName() {
        return "process_id";
    }

    @Override
    protected String getObjectType() {
        return Process.OBJECT_TYPE;
    }

    public void linkToAnotherObject(int objectFromId, String typeObjectFrom, int objectToId, String typeObjectTo,
            String typePrefix, String excludeType) throws BGException {
        try {
            StringBuilder query = new StringBuilder(200);

            query.append(SQL_INSERT_IGNORE);
            query.append(Tables.TABLE_PROCESS_LINK);
            query.append(" (process_id, object_id, object_type, object_title, config) ");
            query.append("SELECT process_id, ?, ?, object_title, config FROM ");
            query.append(Tables.TABLE_PROCESS_LINK);
            query.append(SQL_WHERE);
            query.append("object_id=? AND object_type=?");

            if (Utils.notBlankString(typePrefix)) {
                query.append(" AND object_type LIKE '");
                query.append(typePrefix);
                query.append("%'");
            }

            if (Utils.notBlankString(excludeType)) {
                query.append(" AND object_type NOT LIKE '");
                query.append(excludeType);
                query.append("'");
            }

            PreparedStatement ps = con.prepareStatement(query.toString());
            ps.setInt(1, objectToId);
            ps.setString(2, typeObjectTo);
            ps.setInt(3, objectFromId);
            ps.setString(4, typeObjectFrom);

            ps.executeUpdate();
            ps.close();

            //удаление старых привязок
            query = new StringBuilder(200);

            query.append(SQL_DELETE);
            query.append(Tables.TABLE_PROCESS_LINK);
            query.append(SQL_WHERE);
            query.append("object_id=? AND object_type=?");

            ps = con.prepareStatement(query.toString());
            ps.setInt(1, objectFromId);
            ps.setString(2, typeObjectFrom);

            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            throw new BGException(e);
        }
    }

    /**
     * Возвращает процессы, привязанные к процессу.
     * @param processId код процесса.
     * @param linkType если не null, то SQL LIKE выражение фильтр по типу связи {@link Process#LINK_TYPE_DEPEND}, {@link Process#LINK_TYPE_LINK}, {@link Process#LINK_TYPE_MADE}.
     * @param onlyOpen только открытые.
     * @param typeIds если не null, то фильтр по типам процессов.
     * @return
     * @throws SQLException
     */
    public List<Process> getLinkProcessList(int processId, String linkType, boolean onlyOpen, Set<Integer> typeIds)
            throws SQLException {
        return getFromLinkProcess(processId, linkType, onlyOpen, "INNER JOIN " + TABLE_PROCESS_LINK
                + " AS link ON process.id=link.object_id AND link.object_type LIKE 'process%' AND link.process_id=? ", typeIds);
    }
    
    /**
     * Возвращает процессы, к которым привязан процесс. 
     * @param processId код процесса.
     * @param linkType если не null, то SQL LIKE выражение фильтр по типу связи {@link Process#LINK_TYPE_DEPEND}, {@link Process#LINK_TYPE_LINK}, {@link Process#LINK_TYPE_MADE}. 
     * @param onlyOpen выбирать только открытые процессы.
     * @param typeIds если не null, то фильтр по типам процессов. 
     * @return
     * @throws SQLException
     */
    public List<Process> getLinkedProcessList(int processId, String linkType, boolean onlyOpen, Set<Integer> typeIds)
            throws SQLException {
        return getFromLinkProcess(processId, linkType, onlyOpen,
                "INNER JOIN " + TABLE_PROCESS_LINK + " AS link ON process.id=link.process_id AND link.object_type LIKE 'process%' AND link.object_id=? ",
                typeIds);
    }

    private List<Process> getFromLinkProcess(int processId, String linkType, boolean onlyOpen, String joinQuery,
            Set<Integer> typeIds) throws SQLException {
        List<Process> result = new ArrayList<Process>();

        PreparedDelay pd = new PreparedDelay(con);
        pd.addQuery("SELECT process.* FROM " + TABLE_PROCESS + " AS process ");
        pd.addQuery(joinQuery);
        pd.addQuery(ProcessDAO.getIsolationJoin(user));
        pd.addInt(processId);

        pd.addQuery("WHERE 1>0 ");
        if (CollectionUtils.isNotEmpty(typeIds)) {
            pd.addQuery("AND process.type_id IN (" + Utils.toString(typeIds) + ")");
        }

        if (Utils.notBlankString(linkType)) {
            pd.addQuery("AND link.object_type=?");
            pd.addString(linkType);
        }
        if (onlyOpen) {
            pd.addQuery("AND process.close_dt IS NULL ");
        }
        pd.addQuery("ORDER BY process.create_dt");

        ResultSet rs = pd.executeQuery();
        while (rs.next()) {
            result.add(ProcessDAO.getProcessFromRs(rs));
        }
        pd.close();

        return result;
    }
    
    /**
     * Возвращает связи внутри набора процессов.
     * @param processIds коды процессов из набора.
     * @return
     * @throws SQLException
     */
    public Collection<CommonObjectLink> getLinksOver(Set<Integer> processIds) throws SQLException {
        List<CommonObjectLink> result = new ArrayList<>(processIds.size());

        if (processIds.isEmpty())
            return result;
        
        String ids = Utils.toString(processIds);
        
        StringBuilder query = new StringBuilder(ids.length() * 2 + 300);
        query.append("SELECT * FROM " + TABLE_PROCESS_LINK + " AS link ");
        query.append("WHERE process_id IN (");
        query.append(ids);
        query.append(") AND object_id IN (");
        query.append(ids);
        query.append(") AND link.object_type LIKE 'process%'");
        
        PreparedStatement ps = con.prepareStatement(query.toString());
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            CommonObjectLink link = new CommonObjectLink();
            link.setObjectId(rs.getInt("process_id"));
            link.setLinkedObjectType(rs.getString("object_type"));
            link.setLinkedObjectId(rs.getInt("object_id"));
            result.add(link);
        }
        ps.close();
        
        return result;
    }

    /**
     * Возвращает процессы, привязанные к какой-либо сущности.
     * @param searchResult результат с object_type и привязанным процессом.
     * @param objectType фильтр по типу привязанного объекта LIKE.
     * @param objectId фильтр по коду привязанного объекта.
     * @param objectTitle опциональный фильтр по object_title привязки.
     * @param typeIds опциональный фильтр по типам процессов.
     * @param statusIds опциональный фильтр по статусам процессов.
     * @param paramFilter опциональный фильтр по параметру, передаётся в {@link ParamValueDAO#getParamJoinFilters(String, String)}.
     * @param open опциональный фильтр по открытости процесса.
     * @throws BGException
     */
    public void searchLinkedProcessList(SearchResult<Pair<String, Process>> searchResult, 
            String objectType, int objectId, String objectTitle,
            Set<Integer> typeIds, Set<Integer> statusIds, String paramFilter, Boolean open)
            throws Exception {
        if (searchResult != null) {
            Page page = searchResult.getPage();

            List<Pair<String, Process>> list = searchResult.getList();

            String query = "SELECT SQL_CALC_FOUND_ROWS DISTINCT link.object_type, process.* FROM" + TABLE_PROCESS + " AS process " 
                    + "INNER JOIN " + TABLE_PROCESS_LINK + " AS link ON process.id=link.process_id AND link.object_id=? AND link.object_type LIKE ? "
                    + ProcessDAO.getIsolationJoin(user);
            PreparedDelay pd = new PreparedDelay(con, query);
            
            pd.addInt(objectId);
            pd.addString(objectType);
            
            if (Utils.notBlankString(objectTitle)) {
                pd.addQuery(" AND link.object_title=? ");
                pd.addString(objectTitle);
            }

            if (Utils.notBlankString(paramFilter)) {
                pd.addQuery(ParamValueDAO.getParamJoinFilters(paramFilter, "process.id"));
            }

            if (CollectionUtils.isNotEmpty(typeIds)) {
                pd.addQuery(" AND process.type_id IN (" + Utils.toString(typeIds) + ") ");
            }
            if (CollectionUtils.isNotEmpty(statusIds)) {
                pd.addQuery(" AND process.status_id IN (" + Utils.toString(statusIds) + ") ");
            }

            addOpenFilter(pd, open);

            pd.addQuery(" ORDER BY process.create_dt DESC");
            pd.addQuery(getPageLimit(page));
            
            ResultSet rs = pd.executeQuery();
            while (rs.next()) {
                list.add(new Pair<String, Process>(rs.getString(1), ProcessDAO.getProcessFromRs(rs)));
            }

            PreparedStatement ps = pd.getPrepared();
            if (log.isDebugEnabled()) {
                log.debug(ps);
            }

            if (page != null) {
                page.setRecordCount(getFoundRows(ps));
            }
            pd.close();
        }
    }

    private void addOpenFilter(PreparedDelay pd, Boolean open) {
        if (open != null) {
            if (open)
                pd.addQuery(" AND process.close_dt IS NULL ");
            else
                pd.addQuery(" AND process.close_dt IS NOT NULL ");
        }
    }
    
    @Deprecated
    public void searchLinkedProcessList(SearchResult<Pair<String, Process>> searchResult, 
            String objectType, int objectId, 
            Set<Integer> typeIds, Set<Integer> statusIds, String paramFilter, Boolean closed)
            throws Exception {
        searchLinkedProcessList(searchResult, objectType, objectId, null, typeIds, statusIds, paramFilter, closed);
    }

    /**
     * Возвращает список кодов привязанных к объекту типов процессов.
     * @param objectType SQL LIKE выражение фильтр по типу объекта.
     * @param objectId фильтр по коду объекта. 
     * @return
     * @throws BGException
     */
    public List<Integer> getLinkedProcessTypeIdList(String objectType, int objectId) throws BGException {
        List<Integer> list = new ArrayList<Integer>();

        String query = "SELECT DISTINCT process.type_id FROM " + TABLE_PROCESS + " AS process " + "INNER JOIN "
                + TABLE_PROCESS_LINK
                + " AS link ON process.id=link.process_id  AND link.object_id=? AND link.object_type LIKE ? ";

        query += "ORDER BY process.type_id";
        if (log.isDebugEnabled()) {
            log.debug(query.toString());
        }

        try {
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, objectId);
            ps.setString(2, objectType);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(Utils.parseInt(rs.getString(1)));
            }

            ps.close();
        } catch (SQLException e) {
            throw new BGException(e);
        }

        return list;
    }

    /**
     * Calls {@link #searchLinkProcessList(SearchResult, int, Boolean)} with open = null.
     * @param searchResult
     * @param processId
     * @throws Exception
     */
    public void searchLinkProcessList(SearchResult<Pair<String, Process>> searchResult, int processId)
        throws Exception {
        searchLinkProcessList(searchResult, processId, null);
    }

    /**
     * Searches processes linked to the process.
     * @param searchResult
     * @param processId the process.
     * @param open null or open / close filter.
     * @throws Exception
     */
    public void searchLinkProcessList(SearchResult<Pair<String, Process>> searchResult, int processId, Boolean open)
            throws Exception {
        if (searchResult == null)
            return;

        Page page = searchResult.getPage();
        List<Pair<String, Process>> list = searchResult.getList();

        var pd = new PreparedDelay(con);
        pd.addQuery("SELECT SQL_CALC_FOUND_ROWS DISTINCT link.object_type, process.* FROM " + TABLE_PROCESS_LINK + " AS link ");
        pd.addQuery("INNER JOIN " + TABLE_PROCESS + " AS process ON link.object_id=process.id ");
        pd.addQuery(ProcessDAO.getIsolationJoin(user));
        pd.addQuery("WHERE link.process_id=? AND link.object_type LIKE 'process%' ");
        addOpenFilter(pd, open);
        pd.addQuery("ORDER BY process.create_dt DESC ");
        pd.addQuery(getPageLimit(page));

        pd.addInt(processId);

        ResultSet rs = pd.executeQuery();
        while (rs.next()) {
            list.add(new Pair<String, Process>(rs.getString(1), ProcessDAO.getProcessFromRs(rs)));
        }

        if (page != null)
            page.setRecordCount(getFoundRows(pd.getPrepared()));
    }
    
    /**
     * Проверяет наличие циклических зависимостей.
     * @param processId
     * @return
     */
    public boolean checkCycles(int processId) throws Exception {
        Map<String, Set<Integer>> typeProcessIds = new HashMap<>(CYCLES_CONTROL_LINK_TYPES.size());
        for (String linkType : CYCLES_CONTROL_LINK_TYPES)
            typeProcessIds.put(linkType, Sets.newHashSet(processId));

        Map<String, Set<Integer>> generationTypeProcessIds = typeProcessIds;
        do {
            generationTypeProcessIds = getLinkProcessIds(generationTypeProcessIds);
            for (String linkType : CYCLES_CONTROL_LINK_TYPES) {
                Set<Integer> generationProcessIds = generationTypeProcessIds.get(linkType);
                if (generationProcessIds == null)
                    continue;
                Set<Integer> processIds = typeProcessIds.get(linkType);
                if (!CollectionUtils.intersection(processIds, generationProcessIds).isEmpty())
                    return true;
                processIds.addAll(generationProcessIds);
            }
        } while (generationTypeProcessIds.size() > 0);
        
        return false;
    }
    
    /**
     * @param typeProcessIds
     * @return linked processes, key - relatation type, values - IDs
     * @throws Exception
     */
    private Map<String, Set<Integer>> getLinkProcessIds(Map<String, Set<Integer>> typeProcessIds) throws Exception {
        Map<String, Set<Integer>> result = new HashMap<>(typeProcessIds.size());

        Set<Integer> processIds = typeProcessIds.values().stream().flatMap(Set::stream).collect(Collectors.toSet());

        Statement st = con.createStatement();
        String query = "SELECT process_id, object_type, object_id FROM " + TABLE_PROCESS_LINK + 
                " WHERE process_id IN (" + Utils.toString(processIds) + ") AND object_type LIKE 'process%'";
        ResultSet rs = st.executeQuery(query);
        while (rs.next()) {
            int processId = rs.getInt(1);
            String type = rs.getString(2);

            processIds = typeProcessIds.get(type);
            if (processIds == null || !processIds.contains(processId))
                continue;

            result.computeIfAbsent(type, t -> new HashSet<>()).add(rs.getInt(3));
        }
        st.close();

        log.debug("getLinkProcessIds %s => %s", typeProcessIds, result);
        
        return result;
    }
    
    
    /**
     * Использовать: 
     * Utils.getFirst(getLinkProcessList(Utils.getFirst(linkedProcessList).getId(), Process.LINK_TYPE_DEPEND,
     *           false, Collections.singleton(linkTypeId)))
     */
    @Deprecated
    public Process getProcessLinkedForSame(int processId, int linkedTypeId, int linkTypeId) throws Exception {
        List<Process> linkedProcessList = getLinkedProcessList(processId, Process.LINK_TYPE_DEPEND, false,
                Collections.singleton(linkedTypeId));
        if (linkedProcessList.size() != 1) {
            throw new BGException("Процесс " + processId + " привязан более чем к одному.");
        }

        return Utils.getFirst(getLinkProcessList(Utils.getFirst(linkedProcessList).getId(), Process.LINK_TYPE_DEPEND,
                false, Collections.singleton(linkTypeId)));
    }
    
}
