package ru.bgcrm.plugin.mobile.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bgerp.app.exception.BGException;

import ru.bgcrm.dao.CommonDAO;
import ru.bgcrm.plugin.mobile.model.Account;

public class MobileDAO extends CommonDAO {

    private static final String TABLE_ACCOUNT = " mobile_account ";

    public MobileDAO(Connection con) {
        super(con);
    }

    public void registerAccount(Account account) throws BGException {
        try {
            String query = SQL_DELETE_FROM + TABLE_ACCOUNT + " WHERE object_type=? AND object_id=?";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, account.getObjectType());
            ps.setInt(2, account.getObjectId());
            ps.executeUpdate();
            ps.close();

            query = SQL_INSERT_IGNORE + TABLE_ACCOUNT + "(object_type, object_id, mkey) "
                    + "VALUES (?,?,?)";
            ps = con.prepareStatement(query);
            ps.setString(1, account.getObjectType());
            ps.setInt(2, account.getObjectId());
            ps.setString(3, account.getKey());
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            throw new BGException(e);
        }
    }

    public Account findAccount(String key, String objectType) throws BGException {
        Account result = null;
        try {
            String query = SQL_SELECT_ALL_FROM + TABLE_ACCOUNT + SQL_WHERE + " mkey=? AND object_type=?";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, key);
            ps.setString(2, objectType);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                result = getAccountFromRs(rs);
            }
            ps.close();
        } catch (SQLException ex) {
            throw new BGException(ex);
        }
        return result;
    }

    public Account findAccount(String objectType, int objectId) throws SQLException {
        Account result = null;

        String query = SQL_SELECT_ALL_FROM + TABLE_ACCOUNT + SQL_WHERE + " object_type=? AND object_id=?";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setString(1, objectType);
        ps.setInt(2, objectId);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            result = getAccountFromRs(rs);
        }
        ps.close();

        return result;
    }

    private Account getAccountFromRs(ResultSet rs) throws SQLException {
        Account result = new Account();
        result.setKey(rs.getString("mkey"));
        result.setObjectId(rs.getInt("object_id"));
        result.setObjectType(rs.getString("object_type"));
        return result;
    }

}
