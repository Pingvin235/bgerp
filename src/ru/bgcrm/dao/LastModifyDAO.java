package ru.bgcrm.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import ru.bgcrm.model.LastModify;
import ru.bgcrm.model.LastModifySupport;
import ru.bgcrm.util.TimeUtils;

public class LastModifyDAO
{
	public static final String LAST_MODIFY_COLUMNS = "last_modify_user_id=?, last_modify_dt=?";
	
	public static final void setLastModifyFields( PreparedStatement ps, int pos1, int pos2, LastModify lastModify )
		throws SQLException
	{
		ps.setInt( pos1, lastModify.getUserId() );
		ps.setTimestamp( pos2, TimeUtils.convertDateToTimestamp( lastModify.getTime() ) );
	}
	
	public static void setLastModify( LastModifySupport lmSupport, ResultSet rs )
		throws SQLException
	{
		LastModify result = new LastModify();
		
		result.setUserId( rs.getInt( "last_modify_user_id" ) );
		result.setTime( TimeUtils.convertTimestampToDate( rs.getTimestamp( "last_modify_dt" ) ) );
		
		lmSupport.setLastModify( result );
	}
	
	public static LastModify getLastModify( ResultSet rs )		
	{
		LastModify result = new LastModify();
		
		try 
		{		
			result.setUserId( rs.getInt( "last_modify_user_id" ) );
			result.setTime( TimeUtils.convertTimestampToDate( rs.getTimestamp( "last_modify_dt" ) ) );
		}	
		catch(SQLException e) 
		{
			
		}
	
		return result;
	}
}
