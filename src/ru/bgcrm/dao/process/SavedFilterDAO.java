package ru.bgcrm.dao.process;

import static ru.bgcrm.dao.process.Tables.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import ru.bgcrm.dao.CommonDAO;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.process.queue.config.SavedFilter;
import ru.bgcrm.model.process.queue.config.SavedCommonFiltersConfig;

public class SavedFilterDAO extends CommonDAO {

    public SavedFilterDAO(Connection con) {
        super(con);
    }

    public String getFilterUrlById(int id) throws BGException {
        try {
            String query = "SELECT url FROM " + TABLE_PROCESS_COMMON_FILTER + " WHERE id=?";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getString("url");
            }

            ps.close();
        } catch (SQLException e) {
            throw new BGException(e);
        }
        return "";
    }

    public ArrayList<SavedFilter> getFilters(int queueId) throws BGException {
        try {
            String query = "SELECT * FROM " + TABLE_PROCESS_COMMON_FILTER + " WHERE queue_id = ?";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, queueId);
            ResultSet rs = ps.executeQuery();

            ArrayList<SavedFilter> commonFilters = new ArrayList<SavedFilter>();

            while (rs.next()) {
                commonFilters.add(new SavedFilter(rs.getInt("queue_id"), rs.getInt("id"), rs.getString("title"), rs.getString("url")));
            }

            ps.close();
            return commonFilters;
        } catch (SQLException e) {
            throw new BGException(e);
        }
    }

    public void deleteFilter(int id) throws SQLException {
        log.debug("Deleting " + id);
        String query = "DELETE FROM " + TABLE_PROCESS_COMMON_FILTER + " WHERE id=?";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, id);
        ps.executeUpdate();
        ps.close();
    }

    public void updateFilter(SavedCommonFiltersConfig commonConfig, int queueId) throws SQLException {
        PreparedStatement ps = null;

        for (SavedFilter filter : commonConfig.getQueueSavedCommonFilterSetsMap(queueId)) {
            log.debug("params : " + filter.getTitle());
            String title = filter.getTitle();
            String url = filter.getUrl();
            int id = filter.getId();

            if (id < 0) {
                ps = con.prepareStatement("INSERT INTO " + TABLE_PROCESS_COMMON_FILTER + " (queue_id, title, url) VALUES (?,?,?) ",
                        PreparedStatement.RETURN_GENERATED_KEYS);
                ps.setInt(1, queueId);
                ps.setString(2, title);
                ps.setString(3, url);
                ps.executeUpdate();
                filter.setId(lastInsertId(ps));
                ps.close();
            }
        }
    }
}
