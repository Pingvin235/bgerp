package ru.bgcrm.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Операции со свойствами вида ключ - значение,
 * сохраняемыми в БД.
 */
public class PropertiesDAO extends CommonDAO {
    private static final String TABLE_NAME = " properties ";

    public PropertiesDAO(Connection con) {
        super(con);
    }

    public String get(String key) throws SQLException {
        String result = null;

        String query = "SELECT value FROM" + TABLE_NAME + "WHERE param=?";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setString(1, key);

        ResultSet rs = ps.executeQuery();
        if (rs.next())
            result = rs.getString(1);
        ps.close();

        return result;
    }

    public void set(String key, String value) throws SQLException {
        String query =  SQL_INSERT_INTO + TABLE_NAME +
            "SET param=?, value=?" +
            SQL_ON_DUP_KEY_UPDATE +
            "value=?";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setString(1, key);
        ps.setString(2, value);
        ps.setString(3, value);
        ps.executeUpdate();
        ps.close();
    }
}
