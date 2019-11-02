package ru.bgcrm.dao;

import static ru.bgcrm.dao.Tables.TABLE_EVENT_PROCESSOR_LOG;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.model.EventProcessorLogEntry;
import ru.bgcrm.util.sql.SQLUtils;

public class EventProcessorLogDAO
extends PeriodicDAO
{

	static
	{
		tableNamePrefix = TABLE_EVENT_PROCESSOR_LOG;

		createQuery = " CREATE TABLE " + TABLE_EVENT_PROCESSOR_LOG + " (\n"
					  + "\t`id` INT(11) NOT NULL AUTO_INCREMENT,\n"
					  + "\t`instance_host_name` VARCHAR(50) NOT NULL,\n"
					  + "\t`time` DATETIME NOT NULL,\n"
					  + "\t`event` VARCHAR(250) NOT NULL,\n"
					  + "\t`script` VARCHAR(250) NOT NULL,\n"
					  + "\t`connection_id` INT(10) NOT NULL,\n"
					  + "\t`duration` INT(10) NOT NULL  DEFAULT '-1',\n"
					  + "\t`result_status` TEXT NOT NULL  DEFAULT '',\n"
					  + "\tPRIMARY KEY (`id`),\n"
					  + "\tINDEX `event` (`event`),\n"
					  + "\tINDEX `time` (`time`),\n"
					  + "\tINDEX `script` (`script`),\n"
					  + "\tINDEX `instance_host_name` (`instance_host_name`)\n"
					  + ")";
	}

	public EventProcessorLogDAO( Connection con )
	{
		super( con );
	}

	public int insertLogEntry( EventProcessorLogEntry entry )
	throws BGMessageException
	{
		try
		{
			checkAndCreatePeriodicTable();

			String query = "INSERT INTO " + getMonthTableName( TABLE_EVENT_PROCESSOR_LOG, new Date() ) +
						   "(instance_host_name, time, event, script, connection_id) " +
						   "VALUES (?,NOW(),?,?,?)";

			PreparedStatement ps = con.prepareStatement( query, Statement.RETURN_GENERATED_KEYS );

			int index = 0;
			ps.setString( ++index, entry.getInstanceHostName() );
			ps.setString( ++index, entry.getEvent() );
			ps.setString( ++index, entry.getScript() );
			ps.setInt( ++index, entry.getConnectionId() );

			ps.executeUpdate();

			int logEntryId = lastInsertId( ps );
			ps.close();

			return logEntryId;
		}
		catch( SQLException e )
		{
			throw new BGMessageException( e.getMessage() );
		}
	}

	public void updateLogEntryDuration( int logEntryId, long duration )
	throws BGMessageException
	{
		try
		{
			String query = " UPDATE " + getMonthTableName( TABLE_EVENT_PROCESSOR_LOG, new Date() ) +
						   " SET duration=? " +
						   " WHERE id=?";

			PreparedStatement ps = con.prepareStatement( query );
			ps.setLong( 1, duration );
			ps.setInt( 2, logEntryId );

			ps.executeUpdate();
			ps.close();
		}
		catch( SQLException e )
		{
			throw new BGMessageException( e.getMessage() );
		}
	}

	public void updateLogEntryResultStatus( int logEntryId, String resultStatus )
	throws BGMessageException
	{
		if( SQLUtils.columnExist( con, getMonthTableName( TABLE_EVENT_PROCESSOR_LOG, new Date() ), "result_status" ) )
		{
			try
			{
				String query = " UPDATE " + getMonthTableName( TABLE_EVENT_PROCESSOR_LOG, new Date() ) +
							   " SET result_status=? " +
							   " WHERE id=?";

				PreparedStatement ps = con.prepareStatement( query );

				ps.setString( 1, resultStatus );
				ps.setInt( 2, logEntryId );

				ps.executeUpdate();
				ps.close();
			}
			catch( SQLException e )
			{
				throw new BGMessageException( e.getMessage() );
			}
		}
	}
}

