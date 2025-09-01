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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.bgerp.app.exception.BGException;
import org.bgerp.dao.customer.CustomerDAO;
import org.bgerp.util.sql.LikePattern;
import org.bgerp.util.sql.PreparedQuery;

import com.google.common.collect.Sets;

import ru.bgcrm.dao.CommonLinkDAO;
import ru.bgcrm.dao.Tables;
import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.Pair;
import ru.bgcrm.model.customer.Customer;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;

/**
 * Process links DAO.
 *
 * @author Shamil Vakhitov
 */
public class ProcessLinkDAO extends CommonLinkDAO {
    private static final String LIKE_PROCESS = " LIKE 'process%'";
    private static final Set<String> CYCLES_CONTROL_LINK_TYPES = Set.of(Process.LINK_TYPE_DEPEND, Process.LINK_TYPE_MADE);

    /** User request context for isolations. */
    protected final DynActionForm form;

    /**
     * Constructor without isolations.
     * @param con DB connection.
     */
    public ProcessLinkDAO(Connection con) {
        super(con);
        this.form = null;
    }

    /**
     * Constructor, respecting isolations for methods: {@link #getLinkedProcessList(int, String, boolean, Set)}
     * @param con DB connection.
     * @param form form object with a user.
     */
    public ProcessLinkDAO(Connection con, DynActionForm form) {
        super(con);
        this.form = form;
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
            String typePrefix, String excludeType) {
        try {
            StringBuilder query = new StringBuilder(200);

            query.append(SQL_INSERT_IGNORE_INTO);
            query.append(TABLE_PROCESS_LINK);
            query.append(" (process_id, object_id, object_type, object_title, config) ");
            query.append("SELECT process_id, ?, ?, object_title, config FROM ");
            query.append(TABLE_PROCESS_LINK);
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

            query.append(SQL_DELETE_FROM);
            query.append(TABLE_PROCESS_LINK);
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
                + " AS link ON process.id=link.object_id AND link.object_type " + LIKE_PROCESS + " AND link.process_id=? ", typeIds);
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
                "INNER JOIN " + TABLE_PROCESS_LINK + " AS link ON process.id=link.process_id AND link.object_type " + LIKE_PROCESS + " AND link.object_id=? ",
                typeIds);
    }

    private List<Process> getFromLinkProcess(int processId, String linkType, boolean onlyOpen, String joinQuery,
            Set<Integer> typeIds) throws SQLException {
        List<Process> result = new ArrayList<>();

        PreparedQuery pq = new PreparedQuery(con);
        pq.addQuery("SELECT process.* FROM " + TABLE_PROCESS + " AS process ");
        pq.addQuery(joinQuery);
        pq.addQuery(ProcessDAO.getIsolationJoin(form, "process"));
        pq.addInt(processId);

        pq.addQuery("WHERE 1>0 ");
        if (CollectionUtils.isNotEmpty(typeIds)) {
            pq.addQuery("AND process.type_id IN (" + Utils.toString(typeIds) + ")");
        }

        if (Utils.notBlankString(linkType)) {
            pq.addQuery("AND link.object_type=?");
            pq.addString(linkType);
        }
        if (onlyOpen) {
            pq.addQuery("AND process.close_dt IS NULL ");
        }
        pq.addQuery("ORDER BY process.create_dt");

        ResultSet rs = pq.executeQuery();
        while (rs.next()) {
            result.add(ProcessDAO.getProcessFromRs(rs));
        }
        pq.close();

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
        query.append(") AND link.object_type " + LIKE_PROCESS);

        PreparedStatement ps = con.prepareStatement(query.toString());
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            CommonObjectLink link = new CommonObjectLink();
            link.setObjectId(rs.getInt("process_id"));
            link.setLinkObjectType(rs.getString("object_type"));
            link.setLinkObjectId(rs.getInt("object_id"));
            result.add(link);
        }
        ps.close();

        return result;
    }

    /**
     * Checks cyclic dependencies.
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
     * @return linked processes, key - relation type, values - IDs
     * @throws Exception
     */
    private Map<String, Set<Integer>> getLinkProcessIds(Map<String, Set<Integer>> typeProcessIds) throws Exception {
        Map<String, Set<Integer>> result = new HashMap<>(typeProcessIds.size());

        Set<Integer> processIds = typeProcessIds.values().stream().flatMap(Set::stream).collect(Collectors.toSet());

        Statement st = con.createStatement();
        String query = "SELECT process_id, object_type, object_id FROM " + TABLE_PROCESS_LINK +
                " WHERE process_id IN (" + Utils.toString(processIds) + ") AND object_type " + LIKE_PROCESS;
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

        log.debug("getLinkProcessIds {} => {}", typeProcessIds, result);

        return result;
    }

    /**
     * Selects customers links for process.
     * @param processId process ID.
     * @param linkObjectType optional SQL LIKE filter by link object type.
     * @return
     * @throws SQLException
     */
    public Set<Customer> getLinkCustomers(int processId, String linkObjectType) throws SQLException {
        var result = new TreeSet<Customer>();

        if (Utils.isBlankString(linkObjectType))
            linkObjectType = LikePattern.START.get(Customer.OBJECT_TYPE);

        var query =
            SQL_SELECT + "c.*" + SQL_FROM + Tables.TABLE_CUSTOMER + "AS c" +
            SQL_INNER_JOIN + TABLE_PROCESS_LINK + "AS pl ON c.id=pl.object_id AND pl.process_id=? AND pl.object_type LIKE ?";

        try (var pq = new PreparedQuery(con, query)) {
            pq.addInt(processId);
            pq.addString(linkObjectType);

            var rs = pq.executeQuery();
            while (rs.next())
                result.add(CustomerDAO.getCustomerFromRs(rs, ""));
        }

        return result;
    }

    /**
     * Selects counts of linked and link processes to a given process.
     * Note that process isolation is not taken here on account.
     * @param processId the process ID.
     * @return a pair with linked count on the first place, links count on the second.
     * @throws SQLException
     */
    public Pair<Integer, Integer> getLinkedProcessesCounts(int processId) throws SQLException {
        Pair<Integer, Integer> result = new Pair<>(0, 0);

        String query =
            SQL_SELECT + "COUNT(*)" + SQL_FROM + TABLE_PROCESS_LINK  + "AS linked " +
            SQL_WHERE + "linked.object_id=? AND linked.object_type " + LIKE_PROCESS +
            SQL_UNION_ALL +
            SQL_SELECT + "COUNT(*)" + SQL_FROM + TABLE_PROCESS_LINK + "AS link" +
            SQL_WHERE + "link.process_id=? AND link.object_type " + LIKE_PROCESS;

        try (var pq = new PreparedQuery(con, query)) {
            pq.addInt(processId).addInt(processId);

            var rs = pq.executeQuery();
            if (rs.next())
                result.setFirst(rs.getInt(1));
            if (rs.next())
                result.setSecond(rs.getInt(1));
        }

        return result;
    }

    @Deprecated
    public Set<Integer> getLinkedProcessTypeIdList(String objectType, int objectId) throws SQLException {
        Set<Integer> list = new TreeSet<>();

        String query =
            "SELECT DISTINCT process.type_id FROM " + TABLE_PROCESS + " AS process " +
            "INNER JOIN " + TABLE_PROCESS_LINK + " AS link ON process.id=link.process_id  AND link.object_id=? AND link.object_type LIKE ? ";

        query += "ORDER BY process.type_id";

        try (var ps = con.prepareStatement(query)) {
            ps.setInt(1, objectId);
            ps.setString(2, objectType);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(Utils.parseInt(rs.getString(1)));
            }
        }

        return list;
    }
}
