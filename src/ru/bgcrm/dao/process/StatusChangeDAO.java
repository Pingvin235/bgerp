package ru.bgcrm.dao.process;

import static ru.bgcrm.dao.process.Tables.TABLE_PROCESS_STATUS;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import ru.bgcrm.cache.UserCache;
import ru.bgcrm.dao.CommonDAO;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.model.Page;
import ru.bgcrm.model.SearchResult;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.ProcessType;
import ru.bgcrm.model.process.StatusChange;
import ru.bgcrm.model.process.TransactionProperties;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;

public class StatusChangeDAO extends CommonDAO {
    public StatusChangeDAO(Connection con) {
        super(con);
    }

    public void changeStatus(Process process, ProcessType type, StatusChange change) throws BGException {
        try {
            TransactionProperties transactionProperties = type.getProperties().getTransactionProperties(process.getStatusId(), change.getStatusId());
            if (process.getStatusId() != 0 && !transactionProperties.isEnable()) {
                throw new BGMessageException("Переход со статуса " + process.getStatusId() + " на статус " + change.getStatusId() + " невозможен.");
            }

            ProcessDAO processDAO = new ProcessDAO(con, UserCache.getUser(change.getUserId()), true);
            process.setStatusId(change.getStatusId());
            process.setStatusTime(change.getDate());
            process.setStatusUserId(change.getUserId());
            if (type.getProperties().getCloseStatusIds().contains(change.getStatusId())) {
                process.setCloseTime(change.getDate());
                process.setCloseUserId(change.getUserId());
            } else {
                process.setCloseTime(null);
                process.setCloseUserId(0);
            }
            processDAO.updateProcess(process);

            // флаг last помечает именно "последнесть" конкретного статуса, т.е. может быть несколько last
            String query = "UPDATE " + TABLE_PROCESS_STATUS + " SET last=0 WHERE process_id=? AND status_id=? AND last";

            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, process.getId());
            ps.setInt(2, change.getStatusId());
            ps.executeUpdate();

            ps.close();

            query = "INSERT INTO " + TABLE_PROCESS_STATUS + " (process_id, dt, user_id, status_id, comment) " + "VALUES (?, ?, ?, ?, ?)";

            ps = con.prepareStatement(query.toString());
            ps.setInt(1, process.getId());
            ps.setTimestamp(2, TimeUtils.convertDateToTimestamp(change.getDate()));
            ps.setInt(3, change.getUserId());
            ps.setInt(4, change.getStatusId());
            ps.setString(5, change.getComment());
            ps.executeUpdate();

            ps.close();
        } catch (SQLException e) {
            throw new BGException(e);
        }
    }

    public void searchProcessStatus(SearchResult<StatusChange> searchResult, int processId, Set<Integer> statusIds) throws BGException {
        try {
            if (searchResult != null) {
                Page page = searchResult.getPage();
                List<StatusChange> list = searchResult.getList();

                ResultSet rs = null;
                PreparedStatement ps = null;

                StringBuilder query = new StringBuilder();
                query.append("SELECT SQL_CALC_FOUND_ROWS status_change.* ");
                query.append("FROM ");
                query.append(TABLE_PROCESS_STATUS);
                query.append(" AS status_change ");
                //query.append( "LEFT JOIN " + TABLE_PROCESS_STATUS_TITLE + " AS status ON status_change.status_id=status.id " );
                query.append("WHERE status_change.process_id=? ");
                if (statusIds != null && statusIds.size() > 0) {
                    query.append("AND status_change.status_id IN( ");
                    query.append(Utils.toString(statusIds, "-1", ","));
                    query.append(") ");
                }
                query.append("ORDER BY status_change.dt DESC");
                query.append(getPageLimit(page));

                ps = con.prepareStatement(query.toString());
                ps.setInt(1, processId);
                rs = ps.executeQuery();
                while (rs.next()) {
                    list.add(getProcessStatusFromRs(rs));
                }
                page.setRecordCount(getFoundRows(ps));
                ps.close();
            }
        } catch (SQLException e) {
            throw new BGException(e);
        }
    }

    public List<StatusChange> getProcessStatus(int processId, int statusId) throws BGException {
        List<StatusChange> list = new ArrayList<StatusChange>();

        if (statusId <= 0) {
            return list;
        }

        try {
            ResultSet rs = null;
            PreparedStatement ps = null;

            StringBuilder query = new StringBuilder();
            query.append(" SELECT SQL_CALC_FOUND_ROWS status_change.* ");
            query.append(" FROM ");
            query.append(TABLE_PROCESS_STATUS);
            query.append(" AS status_change ");
            //query.append( " LEFT JOIN " + TABLE_PROCESS_STATUS_TITLE + " AS status ON status_change.status_id=status.id " );
            query.append(" WHERE status_change.process_id=? ");
            query.append(" AND status_change.status_id = ? ");
            query.append(" ORDER BY status_change.dt DESC");

            ps = con.prepareStatement(query.toString());
            ps.setInt(1, processId);
            ps.setInt(2, statusId);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(getProcessStatusFromRs(rs));
            }
            ps.close();
        } catch (SQLException e) {
            throw new BGException(e);
        }
        return list;
    }

    public static StatusChange getProcessStatusFromRs(ResultSet rs) throws SQLException {
        return getProcessStatusFromRs(rs, "status_change.");
    }

    public static StatusChange getProcessStatusFromRs(ResultSet rs, String prefix) throws SQLException {
        StatusChange change = new StatusChange();

        change.setProcessId(rs.getInt(prefix + "process_id"));
        change.setDate(rs.getTimestamp(prefix + "dt"));
        change.setStatusId(rs.getInt(prefix + "status_id"));
        change.setUserId(rs.getInt(prefix + "user_id"));
        change.setComment(rs.getString(prefix + "comment"));

        return change;
    }
}
