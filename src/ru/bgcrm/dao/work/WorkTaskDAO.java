package ru.bgcrm.dao.work;

import static ru.bgcrm.dao.work.Tables.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import ru.bgcrm.dao.CommonDAO;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.work.WorkTask;
import ru.bgcrm.struts.action.WorkAction.ShiftData;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.sql.PreparedDelay;

public class WorkTaskDAO
	extends CommonDAO
{
	public WorkTaskDAO( Connection con )
	{
		super( con );		
	}

	public void loadWorkTask( int graphId, Date date, Map<Integer, List<ShiftData>> dataMap )
		throws BGException
	{
		try
		{
			String query = 
				"SELECT * FROM " + TABLE_CALLBOARD_TASK + 
				" WHERE graph=? AND ?<=time AND time<? " +
				//AND `group` IN(" + Utils.toString( dataMap.keySet() ) + ")
				" ORDER BY slot_from";
			PreparedStatement ps = con.prepareStatement( query );
			ps.setInt( 1, graphId );
			ps.setDate( 2, TimeUtils.convertDateToSqlDate( date ) );
			ps.setDate( 3, TimeUtils.convertDateToSqlDate( TimeUtils.getNextDay( date ) ) );
			
			ResultSet rs = ps.executeQuery();
			
			TASK_LOOP:
			while( rs.next() )
			{
				WorkTask task = getFromRs( rs );
				
				List<ShiftData> dataList = dataMap.get( task.getGroupId() );
				if( dataList != null )
				{
					for( ShiftData dataItem : dataList )
					{
						// либо по бригаде либо по пользователю
						if( (dataItem.getTeam() > 0 && dataItem.getTeam() == task.getTeam()) ||
							(dataItem.getTeam() == 0 && dataItem.getUserIds().contains( task.getUserId() ) ) )
						{
							dataItem.addTask( task );
							continue TASK_LOOP;
						}
					}
				}
			}
			ps.close();
		}
		catch( SQLException e )
		{
			throw new BGException( e );
		}
	}
	
	public WorkTask getTaskByProcessId( int processId )
		throws BGException
	{
		try
		{
			WorkTask result = null;
			
			String query = 
				"SELECT * FROM " + TABLE_CALLBOARD_TASK + " WHERE process_id=?";
			PreparedStatement ps = con.prepareStatement( query );
			
			ps.setInt( 1, processId );
			
			ResultSet rs = ps.executeQuery();
			if( rs.next() )
			{
				result = getFromRs( rs );
			}
			ps.close();
			
			return result;
		}
		catch( Exception e )
		{
			throw new BGException( e );
		}
	}
	
	public void addTask( WorkTask task )
		throws BGException
	{
		try
		{
			String query = null;
			PreparedStatement ps = null;
			
			if( task.getProcessId() != WorkTask.PROCESS_ID_LOCK )
			{
    			query =
    				"UPDATE " + TABLE_CALLBOARD_TASK + " SET graph=?, time=?, slot_from=?, `group`=?, user_id=?, team=?, duration=?, reference=? " +
    				"WHERE process_id=?";
    			ps = con.prepareStatement( query );
    			
    			ps.setInt( 1, task.getGraphId() );
    			ps.setTimestamp( 2, TimeUtils.convertDateToTimestamp( task.getTime() ) );
    			ps.setInt( 3, task.getSlotFrom() );
    			ps.setInt( 4, task.getGroupId() );
    			ps.setInt( 5, task.getUserId() );
    			ps.setInt( 6, task.getTeam() );
    			ps.setInt( 7, task.getDuration() );
    			ps.setString( 8, task.getReference() );
    			ps.setInt( 9, task.getProcessId() );
			
    			boolean updateResult = ps.executeUpdate() > 0;
				ps.close();
				
				if( updateResult )
				{
					return;
				}
			}
			
			query = 
    			"INSERT INTO " + TABLE_CALLBOARD_TASK + " (graph, time, slot_from, `group`, user_id, team, duration, reference, process_id) " +
    			"VALUES (?,?,?,?,?,?,?,?,?)";
			ps = con.prepareStatement( query );

			ps.setInt( 1, task.getGraphId() );
			ps.setTimestamp( 2, TimeUtils.convertDateToTimestamp( task.getTime() ) );
			ps.setInt( 3, task.getSlotFrom() );
			ps.setInt( 4, task.getGroupId() );
			ps.setInt( 5, task.getUserId() );
			ps.setInt( 6, task.getTeam() );
			ps.setInt( 7, task.getDuration() );
			ps.setString( 8, task.getReference() );
			ps.setInt( 9, task.getProcessId() );

			ps.executeUpdate();
			
			ps.close();
		}
		catch( SQLException e )
		{
			throw new BGException( e );
		}
	}
	
	public void removeTask( WorkTask task )
		throws BGException
	{
		try
		{
			PreparedDelay pd = new PreparedDelay( con );
			pd.addQuery( "DELETE FROM " + TABLE_CALLBOARD_TASK + " WHERE graph=? AND `group`=? AND time=? " );
			pd.addInt( task.getGraphId() );
			pd.addInt( task.getGroupId() );
			pd.addTimestamp( TimeUtils.convertDateToTimestamp( task.getTime() ) );
			
			if( task.getTeam() > 0 )
			{
				pd.addQuery( "AND team=?" );
				pd.addInt( task.getTeam() );
			}
			else
			{
				pd.addQuery( "AND user_id=?" );
				pd.addInt( task.getUserId() );
			}			 
			
			pd.executeUpdate();
			pd.close();
		}
		catch( SQLException e )
		{
			throw new BGException( e );
		}
	}
	
	public void removeTaskForProcess( int processId )
		throws BGException
	{
		try
		{
			String query = 
				"DELETE FROM " + TABLE_CALLBOARD_TASK + " WHERE process_id=?";
			PreparedStatement ps = con.prepareStatement( query );
			ps.setInt( 1, processId );
			ps.executeUpdate();
			ps.close();
		}
		catch( SQLException e )
		{
			throw new BGException( e );
		}
	}
		
	private WorkTask getFromRs( ResultSet rs )
		throws SQLException
	{
		WorkTask result = new WorkTask();
		
		result.setGraphId( rs.getInt( "graph" ) );
		result.setTime( TimeUtils.convertTimestampToDate( rs.getTimestamp( "time" ) ) );
		result.setSlotFrom( rs.getInt( "slot_from" ) );
		//result.setSlotTo( rs.getInt( "slot_to" ) );
		result.setGroupId( rs.getInt( "group" ) );
		result.setUserId( rs.getInt( "user_id" ) );		
		result.setTeam( rs.getInt( "team" ) );		
		result.setDuration( rs.getInt( "duration" ) );
		result.setProcessId( rs.getInt( "process_id" ) );		
		result.setReference( rs.getString( "reference" ) );
		
		return result;
	}	
}