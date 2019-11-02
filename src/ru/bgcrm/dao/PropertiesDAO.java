package ru.bgcrm.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import ru.bgcrm.model.BGException;

/**
 * Операции со свойствами вида ключ - значение,
 * сохраняемыми в БД.
 */
public class PropertiesDAO extends CommonDAO {
	
	private static final String TABLE_NAME = " properties ";
	
	public PropertiesDAO(Connection con) {
		super(con);
	}
	
	public String get(String key) throws BGException {
		try {
			String result = null;
			
			String query = "SELECT value FROM" + TABLE_NAME + "WHERE param=?";
			PreparedStatement ps = con.prepareStatement(query);
			ps.setString(1, key);
			
			ResultSet rs = ps.executeQuery();
			if (rs.next())
				result = rs.getString(1);
			ps.close();
			
			return result;
		} catch (SQLException ex) {
			throw new BGException(ex);
		}
	}
	
	public void set(String key, String value) throws BGException {
		try {
			String query =  SQL_INSERT + TABLE_NAME + 
				"SET param=?, value=?" +
				SQL_ON_DUP_KEY_UPDATE + 
				"value=?";
			PreparedStatement ps = con.prepareStatement(query);
			ps.setString(1, key);
			ps.setString(2, value);
			ps.setString(3, value);
			ps.executeUpdate();
			ps.close();			
		} catch (SQLException ex) {
			throw new BGException(ex);
		}
	}
	
}
