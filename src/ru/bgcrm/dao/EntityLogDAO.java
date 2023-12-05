package ru.bgcrm.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import ru.bgcrm.model.EntityLogItem;
import ru.bgcrm.util.Utils;

public class EntityLogDAO extends CommonDAO {
    private String table;

    public EntityLogDAO(Connection con, String table) {
        super(con);
        this.table = table;
    }

    public void insertEntityLog(int id, int userId, String text) throws SQLException {
        if (Utils.notBlankString(text)) {
            String query = SQL_INSERT + table + "(dt, id, user_id, data)" + SQL_VALUES + "(NOW(), ?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, id);
            ps.setInt(2, userId);
            ps.setString(3, text);
            ps.executeUpdate();
            ps.close();
        }
    }

    public List<EntityLogItem> getHistory(int id) throws SQLException {
        List<EntityLogItem> result = new ArrayList<>();

        String query = "SELECT dt, id, user_id, data FROM " + table + " WHERE id=? ORDER BY dt DESC";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            while (rs.next())
                result.add(new EntityLogItem(rs.getTimestamp(1), rs.getInt(2), rs.getInt(3), rs.getString(4)));
        }

        return result;
    }

    public void deleteHistory(int id) throws SQLException {
        String query = SQL_DELETE_FROM + table + SQL_WHERE + "id=?";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}