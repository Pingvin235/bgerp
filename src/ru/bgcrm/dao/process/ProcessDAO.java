package ru.bgcrm.dao.process;

import static ru.bgcrm.dao.process.Tables.TABLE_PROCESS;
import static ru.bgcrm.dao.process.Tables.TABLE_PROCESS_EXECUTOR;
import static ru.bgcrm.dao.process.Tables.TABLE_PROCESS_GROUP;
import static ru.bgcrm.dao.process.Tables.TABLE_PROCESS_LINK;
import static ru.bgcrm.dao.process.Tables.TABLE_PROCESS_STATUS;
import static ru.bgcrm.dao.process.Tables.TABLE_PROCESS_STATUS_TITLE;
import static ru.bgcrm.dao.process.Tables.TABLE_PROCESS_TYPE;
import static ru.bgcrm.dao.user.Tables.TABLE_USER_GROUP;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.bgerp.app.exception.BGException;
import org.bgerp.dao.param.ParamValueDAO;
import org.bgerp.dao.process.ProcessLogDAO;
import org.bgerp.model.Pageable;
import org.bgerp.model.base.IdTitle;
import org.bgerp.model.config.IsolationConfig;
import org.bgerp.model.config.IsolationConfig.IsolationProcess;
import org.bgerp.model.process.ProcessGroups;
import org.bgerp.util.TimeConvert;
import org.bgerp.util.sql.PreparedQuery;

import javassist.NotFoundException;
import ru.bgcrm.dao.CommonDAO;
import ru.bgcrm.dao.message.MessageDAO;
import ru.bgcrm.model.Page;
import ru.bgcrm.model.param.ParameterSearchedObject;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.ProcessExecutor;
import ru.bgcrm.model.process.ProcessGroup;
import ru.bgcrm.model.user.User;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;

public class ProcessDAO extends CommonDAO {
    public static final String LINKED_PROCESS = "linked";

    public static final int MODE_USER_CREATED = 1;
    public static final int MODE_USER_CLOSED = 2;
    public static final int MODE_USER_STATUS_CHANGED = 3;

    /** User request context for isolations, logging changes, l10n. */
    protected final DynActionForm form;

    /**
     * Constructor without user isolation and history writing.
     * @param con DB connection.
     */
    public ProcessDAO(Connection con) {
        super(con);
        this.form = null;
    }

    /**
     * Constructor with isolation support and writing history.
     * @param con DB connection.
     * @param form value of {@link #form}.
     */
    public ProcessDAO(Connection con, DynActionForm form) {
        super(con);
        this.form = form;
    }

    public static Process getProcessFromRs(ResultSet rs, String prefix) throws SQLException {
        Process process = new Process();

        process.setId(rs.getInt(prefix + "id"));
        process.setDescription(rs.getString(prefix + "description"));
        process.setTypeId(rs.getInt(prefix + "type_id"));
        process.setStatusId(rs.getInt(prefix + "status_id"));
        process.setStatusUserId(rs.getInt(prefix + "status_user_id"));
        process.setCreateUserId(rs.getInt(prefix + "create_user_id"));
        process.setCloseUserId(rs.getInt(prefix + "close_user_id"));
        process.setPriority(rs.getInt(prefix + "priority"));
        process.setCreateTime(rs.getTimestamp(prefix + "create_dt"));
        process.setCloseTime(rs.getTimestamp(prefix + "close_dt"));
        process.setStatusTime(rs.getTimestamp(prefix + "status_dt"));

        List<IdTitle> idTitle = Utils.parseIdTitleList(rs.getString(prefix + "groups"), "0");
        ProcessGroups processGroups = new ProcessGroups();

        for (IdTitle item : idTitle) {
            ProcessGroup processGroup = new ProcessGroup();
            processGroup.setGroupId(item.getId());
            processGroup.setRoleId(Integer.parseInt(item.getTitle()));

            processGroups.add(processGroup);
        }

        process.setGroups(processGroups);
        process.setExecutors(ProcessExecutor.parseSafe(rs.getString(prefix + "executors"), processGroups));

        return process;
    }

    public static Process getProcessFromRs(ResultSet rs) throws SQLException {
        return getProcessFromRs(rs, "process.");
    }

    public static String getIsolationJoin(DynActionForm form, String tableProcess) {
        if (form == null)
            return "";

        User user = form.getUser();

        IsolationProcess isolation = user.getConfigMap().getConfig(IsolationConfig.class).getIsolationProcess();
        if (isolation == IsolationProcess.EXECUTOR)
            return " INNER JOIN " + TABLE_PROCESS_EXECUTOR
                    + " AS isol_e ON " + tableProcess + ".id=isol_e.process_id AND isol_e.user_id=" + user.getId() + " ";
        if (isolation == IsolationProcess.GROUP) {
            var result = " INNER JOIN " + TABLE_PROCESS_GROUP + " AS isol_pg ON " + tableProcess + ".id=isol_pg.process_id "
                    + "INNER JOIN " + TABLE_USER_GROUP
                    + " AS isol_ur ON isol_ur.group_id=isol_pg.group_id AND isol_ur.user_id=" + user.getId()
                    + " AND (isol_ur.date_to IS NULL OR CURDATE()<=isol_ur.date_to) ";
            if (StringUtils.isNotBlank(isolation.getExecutorTypeIds())) {
                result += " INNER JOIN " + TABLE_PROCESS + " AS isol_ge ON " + tableProcess + ".id=isol_ge.id AND ("
                    + tableProcess + ".type_id NOT IN (" + isolation.getExecutorTypeIds() + ") "
                    + "OR isol_ge.executors LIKE '" + user.getId() + ":%'"
                    + "OR POSITION(', " + user.getId() + ":' IN isol_ge.executors) > 0 "
                    // for future case of changing store format without white spaces
                    + "OR POSITION('," + user.getId() + ":' IN isol_ge.executors) > 0 "
                    + " )";
            }
            return result;
        }
        return "";
    }

    /**
     * Selects process by ID with the last {@link Process#getStatusChange()}.
     * Selection respects user isolations.
     * @param id DB record ID.
     * @return
     * @throws SQLException
     */
    public Process getProcess(int id) throws SQLException {
        Process result = null;

        String query = "SELECT process.*, ps.* " + SQL_FROM + TABLE_PROCESS + " AS process "
                + "LEFT JOIN " + TABLE_PROCESS_STATUS
                + " AS ps ON process.id=ps.process_id AND ps.status_id=process.status_id AND ps.last "
                + getIsolationJoin(form, "process")
                + SQL_WHERE + "process.id=?";
        var ps = con.prepareStatement(query);
        ps.setInt(1, id);
        var rs = ps.executeQuery();
        if (rs.next()) {
            result = getProcessFromRs(rs);
            result.setStatusChange(StatusChangeDAO.getProcessStatusFromRs(rs, "ps."));
        }
        ps.close();

        return result;
    }

    /**
     * Selects process using {@link #getProcess(int)}.
     * @param id DB record ID.
     * @return
     * @throws SQLException
     * @throws NotFoundException no record found with {@code id}.
     */
    public Process getProcessOrThrow(int id) throws SQLException, NotFoundException {
        var result = getProcess(id);
        if (result == null)
            throw new NotFoundException("Process not found: " + id);
        return result;
    }

    /** Use {@link org.bgerp.dao.process.ProcessSearchDAO} */
    @Deprecated
    public List<Process> getProcessList(Collection<Integer> processIds) {
        List<Process> processList = new ArrayList<Process>();
        try {
            String query = "SELECT process.* " + SQL_FROM + TABLE_PROCESS + " AS process " + "WHERE process.id IN ( "
                    + Utils.toString(processIds) + ")";
            PreparedStatement ps = con.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                processList.add(getProcessFromRs(rs));
            }
            ps.close();

            return processList;
        } catch (SQLException e) {
            throw new BGException(e);
        }
    }

    public void updateProcessGroups(Set<ProcessGroup> processGroups, int processId) throws SQLException {
        if (form != null) {
            Process oldValue = new ProcessDAO(con).getProcess(processId);
            Process newValue = oldValue.clone();
            newValue.setGroups(new ProcessGroups(processGroups));
            logProcessChange(newValue, oldValue);
        }

        updateColumn(TABLE_PROCESS, processId, "groups", ProcessGroup.serialize(processGroups));

        String query = SQL_DELETE_FROM + Tables.TABLE_PROCESS_GROUP + SQL_WHERE + "process_id=?";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, processId);
        ps.executeUpdate();
        ps.close();

        query = SQL_INSERT_INTO + Tables.TABLE_PROCESS_GROUP + "VALUES (?, ?, ?)";
        ps = con.prepareStatement(query);
        ps.setInt(1, processId);

        for (ProcessGroup item : processGroups) {
            ps.setInt(2, item.getGroupId());
            ps.setInt(3, item.getRoleId());
            ps.executeUpdate();
        }

        ps.close();
    }

    public void updateProcessExecutors(Set<ProcessExecutor> processExecutors, int processId) throws SQLException {
        if (form != null) {
            Process oldValue = new ProcessDAO(con).getProcess(processId);
            Process newValue = oldValue.clone();
            newValue.setExecutors(processExecutors);
            logProcessChange(newValue, oldValue);
        }

        updateColumn(TABLE_PROCESS, processId, "executors", ProcessExecutor.serialize(processExecutors));

        String query = SQL_DELETE_FROM + Tables.TABLE_PROCESS_EXECUTOR + SQL_WHERE + "process_id=?";

        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, processId);
        ps.executeUpdate();
        ps.close();

        query = "INSERT INTO " + Tables.TABLE_PROCESS_EXECUTOR
                + " ( process_id, group_id, role_id, user_id ) VALUES ( ?, ?, ?, ? ) ";
        ps = con.prepareStatement(query);
        ps.setInt(1, processId);

        for (ProcessExecutor processExecutor : processExecutors) {
            ps.setInt(2, processExecutor.getGroupId());
            ps.setInt(3, processExecutor.getRoleId());
            ps.setInt(4, processExecutor.getUserId());
            ps.executeUpdate();
        }

        ps.close();
    }

    private void logProcessChange(Process process, Process oldProcess) throws SQLException {
        new ProcessLogDAO(this.con).insertEntityLog(process.getId(), form.getUserId(), process.getChangesLog(oldProcess));
    }

    public Process updateProcess(Process process) throws SQLException {
        if (process != null) {
            Process oldProcess = getProcess(process.getId());
            if (form != null && oldProcess != null && !oldProcess.isEqualProperties(process)) {
                logProcessChange(process, oldProcess);
            }

            int index = 1;
            PreparedStatement ps = null;
            StringBuilder query = new StringBuilder();
            // раньше была проверка на положительный ID, но он может быть отрицательным в случае, если процесс временный
            if (oldProcess != null) {
                query.append(SQL_UPDATE + TABLE_PROCESS
                        + " SET status_id=?, status_dt=?, status_user_id=?, description=?, close_dt=?, priority=?, close_user_id=?, type_id=? WHERE id=?");
                ps = con.prepareStatement(query.toString());
                ps.setInt(index++, process.getStatusId());
                ps.setTimestamp(index++, TimeConvert.toTimestamp(process.getStatusTime()));
                ps.setInt(index++, process.getStatusUserId());
                ps.setString(index++, process.getDescription());
                ps.setTimestamp(index++, TimeConvert.toTimestamp(process.getCloseTime()));
                ps.setInt(index++, process.getPriority());
                ps.setInt(index++, process.getCloseUserId());
                ps.setInt(index++, process.getTypeId());
                ps.setInt(index++, process.getId());
                ps.executeUpdate();

            } else {
                if (process.getCreateTime() == null)
                    process.setCreateTime(new Date());

                query.append("INSERT INTO " + TABLE_PROCESS
                        + " SET type_id=?, status_id=?, status_user_id=?, status_dt=?, description=?, create_dt=?, executors=?, create_user_id=?");
                ps = con.prepareStatement(query.toString(), PreparedStatement.RETURN_GENERATED_KEYS);
                ps.setInt(index++, process.getTypeId());
                ps.setInt(index++, process.getStatusId());
                ps.setInt(index++, process.getStatusUserId());
                ps.setTimestamp(index++, TimeConvert.toTimestamp(process.getCreateTime()));
                ps.setString(index++, process.getDescription());
                ps.setTimestamp(index++, TimeConvert.toTimestamp(process.getCreateTime()));
                ps.setString(index++, ProcessExecutor.serialize(process.getExecutors()));
                ps.setInt(index++, process.getCreateUserId());
                ps.executeUpdate();
                process.setId(lastInsertId(ps));
            }
            ps.close();
        }
        return process;
    }

    public void deleteProcess(int processId) throws SQLException {
        deleteProcessData(processId, SQL_DELETE_FROM + TABLE_PROCESS + SQL_WHERE + "id=?");
        deleteProcessData(processId, SQL_DELETE_FROM + TABLE_PROCESS_GROUP + SQL_WHERE + "process_id=?");
        deleteProcessData(processId, SQL_DELETE_FROM + TABLE_PROCESS_EXECUTOR + SQL_WHERE + "process_id=?");

        deleteProcessData(processId, SQL_DELETE_FROM + TABLE_PROCESS_LINK + SQL_WHERE + "process_id=?");
        deleteProcessData(processId, SQL_DELETE_FROM + TABLE_PROCESS_LINK + SQL_WHERE + "object_id=? AND object_type LIKE 'process%'");

        new ParamValueDAO(con).deleteParams(Process.OBJECT_TYPE, processId);

        new MessageDAO(con).deleteProcessMessages(processId);

        new ProcessLogDAO(this.con).deleteHistory(processId);
    }

    private void deleteProcessData(int processId, String query) throws SQLException {
        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, processId);
        ps.executeUpdate();
        ps.close();
    }

    public void processIdInvert(Process process) throws SQLException {
        int currentProcessId = process.getId();

        updateProcessId(currentProcessId, SQL_UPDATE + TABLE_PROCESS + " SET id=? WHERE id=?");
        updateProcessId(currentProcessId, SQL_UPDATE + TABLE_PROCESS_GROUP + " SET process_id=? WHERE process_id=?");
        updateProcessId(currentProcessId, SQL_UPDATE + TABLE_PROCESS_EXECUTOR + " SET process_id=? WHERE process_id=?");

        updateProcessId(currentProcessId, SQL_UPDATE + TABLE_PROCESS_LINK + " SET process_id=? WHERE process_id=?");
        updateProcessId(currentProcessId, SQL_UPDATE + TABLE_PROCESS_LINK + " SET object_id=? WHERE object_id=? AND object_type LIKE 'process%'");

        new ParamValueDAO(con).objectIdInvert(Process.OBJECT_TYPE, currentProcessId);

        process.setId(-currentProcessId);
    }

    private void updateProcessId(int currentProcessId, String query) throws SQLException {
        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, -currentProcessId);
        ps.setInt(2, currentProcessId);
        ps.executeUpdate();
        ps.close();
    }

    /**
     * Search processes by 'address' param.
     * @param searchResult result
     * @param addressParamIds param IDs used for search.
     * @param houseId house ID
     * @param houseFlat flat
     * @param houseRoom room
     * @throws SQLException
     */
    public void searchProcessListByAddress(Pageable<ParameterSearchedObject<Process>> searchResult,
            Set<Integer> typeIds, Set<Integer> addressParamIds, int houseId, String houseFlat, String houseRoom)
                    throws SQLException {
        if (searchResult != null) {
            Page page = searchResult.getPage();
            List<ParameterSearchedObject<Process>> list = searchResult.getList();

            PreparedQuery ps = new PreparedQuery(con);
            String ids = Utils.toString(addressParamIds);

            ps.addQuery(SQL_SELECT_COUNT_ROWS);
            ps.addQuery("DISTINCT param.param_id, param.value, process.*, type.title, status.title ");
            ps.addQuery(SQL_FROM);
            ps.addQuery(TABLE_PROCESS);
            ps.addQuery("AS process");

            ps.addQuery(SQL_INNER_JOIN);
            ps.addQuery(org.bgerp.dao.param.Tables.TABLE_PARAM_ADDRESS);
            ps.addQuery("AS param ON c.id=param.id AND param.param_id IN (");
            ps.addQuery(ids);
            ps.addQuery(")");

            ps.addQuery(" AND param.house_id=?");
            ps.addInt(houseId);

            if (Utils.notBlankString(houseFlat)) {
                ps.addQuery(" AND param.flat=?");
                ps.addString(houseFlat);
            }
            if (Utils.notBlankString(houseRoom)) {
                ps.addQuery(" AND param.room=?");
                ps.addString(houseRoom);
            }

            ps.addQuery(" LEFT JOIN " + TABLE_PROCESS_TYPE + " AS type ON process.type_id=type.id ");
            ps.addQuery(
                    " LEFT JOIN " + TABLE_PROCESS_STATUS_TITLE + " AS status ON status.id = process.status_id ");

            ps.addQuery(SQL_WHERE + "1>0 ");
            if (typeIds != null && typeIds.size() > 0) {
                ps.addQuery(" AND process.type_id IN ");
                ps.addQuery(Utils.toString(typeIds));
                ps.addQuery(" )");
            }

            ps.addQuery(SQL_ORDER_BY);
            ps.addQuery("p.create_dt");
            ps.addQuery(getPageLimit(page));

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new ParameterSearchedObject<>(getProcessFromRs(rs), rs.getInt(1), rs.getString(2)));
            }

            setRecordCount(page, ps.getPrepared());
            ps.close();
        }
    }

    /**
     * Searches processes with user as an executor.
     * @param searchResult paged result.
     * @param userId user ID.
     * @param open if not {@code null} then process opened filter.
     * @throws SQLException
     */
    public void searchProcessListForUser(Pageable<Process> searchResult, int userId, Boolean open) throws SQLException {
        Page page = searchResult.getPage();
        List<Process> list = searchResult.getList();

        PreparedQuery pq = new PreparedQuery(con);

        pq.addQuery(SQL_SELECT_COUNT_ROWS);
        pq.addQuery("DISTINCT p.*");
        pq.addQuery(SQL_FROM);
        pq.addQuery(TABLE_PROCESS);
        pq.addQuery("AS p ");
        pq.addQuery(SQL_INNER_JOIN);
        pq.addQuery(TABLE_PROCESS_EXECUTOR);
        pq.addQuery("AS e ON e.process_id=p.id AND e.user_id=?");
        pq.addInt(userId);
        addOpenFilter(pq, open);
        pq.addQuery(SQL_ORDER_BY);
        pq.addQuery("create_dt DESC");

        pq.addQuery(getPageLimit(page));

        ResultSet rs = pq.executeQuery();
        while (rs.next())
            list.add(getProcessFromRs(rs, ""));
        setRecordCount(page, pq.getPrepared());
        pq.close();
    }

    private void addOpenFilter(PreparedQuery pq, Boolean open) {
        if (open != null) {
            if (open) {
                pq.addQuery(SQL_WHERE + "close_dt IS NULL ");
            } else {
                pq.addQuery(SQL_WHERE + "close_dt IS NOT NULL ");
            }
        }
    }

    /**
     * Выбирает связанные с процессом процессы.
     * @param searchResult
     * @param userId код пользователя.
     * @param mode принимает значения {@link #MODE_USER_CREATED}, {@link #MODE_USER_CLOSED}, {@link #MODE_USER_STATUS_CHANGED}.
     * @throws SQLException
     */
    public void searchProcessListForUser(Pageable<Process> searchResult, int userId, int mode) throws SQLException {
        if (searchResult != null) {
            Page page = searchResult.getPage();
            List<Process> list = searchResult.getList();

            PreparedQuery pq = new PreparedQuery(con);

            pq.addQuery(SQL_SELECT_COUNT_ROWS);
            pq.addQuery("*");
            pq.addQuery(SQL_FROM);
            pq.addQuery(TABLE_PROCESS + " AS p ");
            pq.addQuery(getIsolationJoin(form, "p"));

            final String groupBy = SQL_GROUP_BY + "p.id ";

            if (mode == MODE_USER_CREATED) {
                pq.addQuery("WHERE p.create_user_id=?");
                pq.addInt(userId);
                pq.addQuery(" AND p.close_dt is NULL");
                pq.addQuery(groupBy);
                pq.addQuery(SQL_ORDER_BY);
                pq.addQuery("p.create_dt DESC");
            } else if (mode == MODE_USER_CLOSED) {
                pq.addQuery("WHERE p.close_user_id=?");
                pq.addInt(userId);
                pq.addQuery(groupBy);
                pq.addQuery(SQL_ORDER_BY);
                pq.addQuery("p.close_dt DESC");
            } else if (mode == MODE_USER_STATUS_CHANGED) {
                pq.addQuery("WHERE p.status_user_id=?");
                pq.addInt(userId);
                pq.addQuery(groupBy);
                pq.addQuery(SQL_ORDER_BY);
                pq.addQuery("p.status_dt DESC");
            }

            pq.addQuery(getPageLimit(page));

            ResultSet rs = pq.executeQuery();
            while (rs.next())
                list.add(getProcessFromRs(rs, "p."));

            setRecordCount(page, pq.getPrepared());
            pq.close();
        }
    }
}
