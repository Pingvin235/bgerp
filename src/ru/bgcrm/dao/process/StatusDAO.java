package ru.bgcrm.dao.process;

import static ru.bgcrm.dao.process.Tables.TABLE_PROCESS_STATUS_TITLE;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bgerp.model.Pageable;

import ru.bgcrm.dao.CommonDAO;
import ru.bgcrm.model.Page;
import ru.bgcrm.model.process.Status;
import ru.bgcrm.util.Utils;

public class StatusDAO extends CommonDAO {
    public StatusDAO(Connection con) {
        super(con);
    }

    public void searchStatus(Pageable<Status> searchResult) throws SQLException {
        if (searchResult != null) {
            Page page = searchResult.getPage();
            List<Status> list = searchResult.getList();

            ResultSet rs = null;
            PreparedStatement ps = null;
            StringBuilder query = new StringBuilder();
            query.append(SQL_SELECT_COUNT_ROWS);
            query.append("*");
            query.append(SQL_FROM);
            query.append(TABLE_PROCESS_STATUS_TITLE);
            query.append(SQL_ORDER_BY);
            query.append("pos, title");
            query.append(getPageLimit(page));
            ps = con.prepareStatement(query.toString());
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(getStatusFromRs(rs));
            }
            page.setRecordCount(ps);
            ps.close();
        }
    }

    public Status getStatus(int id) throws SQLException {
        Status result = null;

        ResultSet rs = null;
        PreparedStatement ps = null;
        ps = con.prepareStatement("SELECT * FROM " + TABLE_PROCESS_STATUS_TITLE + " WHERE id=?");
        ps.setInt(1, id);
        rs = ps.executeQuery();
        while (rs.next()) {
            result = getStatusFromRs(rs);
        }
        ps.close();

        return result;
    }

    public void deleteStatus(int id) throws SQLException {
        PreparedStatement ps = null;

        StringBuilder query = new StringBuilder();
        query.append(SQL_DELETE_FROM);
        query.append(TABLE_PROCESS_STATUS_TITLE);
        query.append(SQL_WHERE);
        query.append("id=?");

        ps = con.prepareStatement(query.toString());
        ps.setInt(1, id);
        ps.executeUpdate();

        ps.close();
    }

    public void updateStatus(Status status) throws SQLException {
        int index = 1;
        PreparedStatement ps = null;

        if (status.getId() < 0) {
            ps = con.prepareStatement("INSERT INTO " + TABLE_PROCESS_STATUS_TITLE + " SET title=?, pos=?", PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(index++, status.getTitle());
            ps.setInt(index++, status.getPos());
            ps.executeUpdate();
            status.setId(lastInsertId(ps));
        } else {
            ps = con.prepareStatement("UPDATE " + TABLE_PROCESS_STATUS_TITLE + " SET title=?, pos=? WHERE id=?");
            ps.setString(index++, status.getTitle());
            ps.setInt(index++, status.getPos());
            ps.setInt(index++, status.getId());
            ps.executeUpdate();
        }
        ps.close();
    }

    public List<Status> getStatusList() throws SQLException {
        List<Status> result = new ArrayList<>();

        ResultSet rs = null;
        PreparedStatement ps = null;

        ps = con.prepareStatement("SELECT * FROM " + TABLE_PROCESS_STATUS_TITLE + " ORDER BY pos ");
        rs = ps.executeQuery();
        while (rs.next()) {
            result.add(getStatusFromRs(rs));
        }
        ps.close();

        return result;
    }

    public List<Status> getStatusList(Set<Integer> ids) throws SQLException {
        List<Status> result = new ArrayList<>();

        ResultSet rs = null;
        PreparedStatement ps = null;

        StringBuilder query = new StringBuilder("SELECT * FROM " + TABLE_PROCESS_STATUS_TITLE);
        if (ids.size() > 0) {
            query.append(" WHERE id IN(");
            query.append(Utils.toString(ids));
            query.append(") ");
        }
        query.append(" ORDER BY pos");

        ps = con.prepareStatement(query.toString());
        rs = ps.executeQuery();
        while (rs.next()) {
            result.add(getStatusFromRs(rs));
        }
        ps.close();

        return result;
    }

    public static Status getStatusFromRs(ResultSet rs) throws SQLException {
        Status result = new Status();
        result.setId(rs.getInt("id"));
        result.setTitle(rs.getString("title"));
        result.setPos(rs.getInt("pos"));
        return result;
    }
}