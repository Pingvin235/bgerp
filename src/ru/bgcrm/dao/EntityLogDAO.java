package ru.bgcrm.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import ru.bgcrm.model.BGException;
import ru.bgcrm.model.EntityLogItem;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;

public class EntityLogDAO extends CommonDAO {
    private String table;

    public EntityLogDAO(Connection con, String table) {
        super(con);
        this.table = table;
    }

    public void insertEntityLog(int id, int userId, String text) throws SQLException {
        if (Utils.notBlankString(text)) {
            String query = "INSERT INTO" + table + "( dt, id, user_id, data) VALUES( NOW(), ?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, id);
            ps.setInt(2, userId);
            ps.setString(3, text);
            ps.executeUpdate();
            ps.close();
        }
    }

    public List<EntityLogItem> getHistory(int id) throws BGException {
        List<EntityLogItem> entityList = new ArrayList<EntityLogItem>();

        try {
            String query = "SELECT dt, id, user_id, data FROM " + table + " WHERE id=? ORDER BY dt DESC";
            PreparedStatement ps = con.prepareStatement(query);

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                entityList.add(new EntityLogItem(TimeUtils.convertTimestampToDate(rs.getTimestamp(1)), rs.getInt(2),
                        rs.getInt(3), rs.getString(4)));
            }
            ps.close();

        } catch (SQLException e) {
            throw new BGException(e);
        }

        return entityList;
    }

    public void deleteHistory(int id) throws BGException {
        try {
            String query = "DELETE FROM " + table + " WHERE id=?";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, id);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            throw new BGException(e);
        }
    }
}