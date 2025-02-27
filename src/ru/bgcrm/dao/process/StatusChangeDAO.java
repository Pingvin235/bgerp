package ru.bgcrm.dao.process;

import static ru.bgcrm.dao.process.Tables.TABLE_PROCESS_STATUS;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bgerp.app.exception.BGException;
import org.bgerp.model.Pageable;
import org.bgerp.util.TimeConvert;

import ru.bgcrm.dao.CommonDAO;
import ru.bgcrm.model.Page;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.ProcessType;
import ru.bgcrm.model.process.StatusChange;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;

public class StatusChangeDAO extends CommonDAO {
    private final DynActionForm form;

    public StatusChangeDAO(Connection con) {
        super(con);
        this.form = null;
    }

    public StatusChangeDAO(Connection con, DynActionForm form) {
        super(con);
        this.form = form;
    }

    public void changeStatus(Process process, ProcessType type, StatusChange change) throws SQLException {
        ProcessDAO processDAO = new ProcessDAO(con, form);
        process.setStatusId(change.getStatusId());
        process.setStatusTime(change.getDate());
        process.setStatusUserId(change.getUserId());
        if (type != null && type.getProperties().getCloseStatusIds().contains(change.getStatusId())) {
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
        ps.setTimestamp(2, TimeConvert.toTimestamp(change.getDate()));
        ps.setInt(3, change.getUserId());
        ps.setInt(4, change.getStatusId());
        ps.setString(5, change.getComment());
        ps.executeUpdate();

        ps.close();
    }

    public void searchProcessStatus(Pageable<StatusChange> searchResult, int processId, Set<Integer> statusIds) {
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
                query.append(page.getLimitSql());

                ps = con.prepareStatement(query.toString());
                ps.setInt(1, processId);
                rs = ps.executeQuery();
                while (rs.next()) {
                    list.add(getProcessStatusFromRs(rs));
                }
                page.setRecordCount(ps);
                ps.close();
            }
        } catch (SQLException e) {
            throw new BGException(e);
        }
    }

    public List<StatusChange> getProcessStatus(int processId, int statusId) {
        List<StatusChange> list = new ArrayList<>();

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
