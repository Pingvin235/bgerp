package ru.bgcrm.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.bgerp.model.Pageable;

import ru.bgcrm.model.ObjectLogRecord;
import ru.bgcrm.model.Page;
import ru.bgcrm.util.TimeUtils;

public class ObjectLogDAO
    extends CommonDAO
{
	public ObjectLogDAO( Connection con )
	{
		super( con );
	}

	public void searchObjectLogList( Pageable<ObjectLogRecord> searchResult, int userId )
	    throws SQLException
	{
		if( searchResult != null )
		{
			Page page = searchResult.getPage();
			List<ObjectLogRecord> list = searchResult.getList();
			int index = 1;
			ResultSet rs = null;
			PreparedStatement ps = null;
			StringBuilder query = new StringBuilder();

			query.append( "SELECT SQL_CALC_FOUND_ROWS * FROM object_log WHERE 1=1" );
			if( userId > 0 )
			{
				query.append( " AND user_id=?" );
			}
			query.append( " ORDER BY dt DESC" );
			query.append( getPageLimit( page ) );
			ps = con.prepareStatement( query.toString() );
			if( userId > 0 )
			{
				ps.setInt( index++, userId );
			}
			rs = ps.executeQuery();
			while( rs.next() )
			{
				ObjectLogRecord logRecord = new ObjectLogRecord();
				logRecord.setId( rs.getInt( "id" ) );
				logRecord.setDate( rs.getTimestamp( "dt" ) );
				logRecord.setObjectType( rs.getString( "object_type" ) );
				logRecord.setObjectId( rs.getInt( "object_id" ) );
				logRecord.setTitle( rs.getString( "title" ) );
				list.add( logRecord );
			}
			rs.close();
			if( page != null )
			{
				page.setRecordCount( foundRows( ps ) );
			}
			ps.close();
		}
	}

	public void updateLog( ObjectLogRecord logRecord )
	    throws SQLException
	{
		if( logRecord != null )
		{
			int index = 1;
			PreparedStatement ps = null;
			StringBuilder query = new StringBuilder();

			query.append( "INSERT INTO object_log SET dt=?, user_id=?, object_type=?, object_id=?, title=?" );
			ps = con.prepareStatement( query.toString() );
			ps.setTimestamp( index++, TimeUtils.convertDateToTimestamp( logRecord.getDate() ) );
			ps.setInt( index++, logRecord.getUserId() );
			ps.setString( index++, logRecord.getObjectType() );
			ps.setInt( index++, logRecord.getObjectId() );
			ps.setString( index++, logRecord.getTitle() );
			ps.executeUpdate();
			ps.close();
		}
	}

    protected void updateObjectLog( String objectType, int objectId, String title, int userId, Connection con )
    	throws SQLException
    {
    	ObjectLogDAO objectLogDAO = new ObjectLogDAO( con );
    	ObjectLogRecord logRecord = new ObjectLogRecord();
    	logRecord.setDate( new Date() );
    	logRecord.setObjectType( objectType );
    	logRecord.setObjectId( objectId );
    	logRecord.setTitle( title );
    	logRecord.setUserId( userId );
    	objectLogDAO.updateLog( logRecord );
    }
}