package ru.bgcrm.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import ru.bgcrm.model.BGMessageException;

public class PeriodicDAO
	extends CommonDAO
{
	protected static Set<String> periodicTableSet = Collections.newSetFromMap( new ConcurrentHashMap<String, Boolean>() );
	protected static String createQuery;
	protected static String tableNamePrefix;

	protected PeriodicDAO( Connection con )
	{
		super( con );
	}

	protected void checkAndCreatePeriodicTable()
		throws BGMessageException
	{
		String table = getMonthTableName( tableNamePrefix, new Date() );
		if( !tableExists( table ) )
		{
			try
			{
				PreparedStatement ps = con.prepareStatement( createQuery.replace( tableNamePrefix, getMonthTableName( tableNamePrefix, new Date() ) ).toString() );
				ps.executeUpdate();
				ps.close();
			}
			catch( SQLException e )
			{
				throw new BGMessageException( e.getMessage() );
			}
		}
	}

	protected String getMonthTableName( String name, Date time )
	{
		SimpleDateFormat getModuleMonthTableNameFormat = new SimpleDateFormat( "_yyyyMM" );
		StringBuilder sb = new StringBuilder( name.trim() );
		sb.append( getModuleMonthTableNameFormat.format( time ) );
		return sb.toString();
	}

	/**
	 * Проверка на существование таблицы в БД
	 * @param con объект доступа к БД
	 * @param tableName имя проверяемой таблицы
	 * @return true - таблица существует, false - таблица не существует
	 * или нет доступа к БД
	 * @throws SQLException если возникают проблемы с доступом к БД
	 */
	public boolean tableExists( String tableName )
	{
		boolean result = false;
		try
		{
			if( periodicTableSet.contains( tableName ) )
			{
				result = true;
			}
			else
			{
				if( con != null && tableName != null )
				{
					String query = "SHOW TABLES LIKE ?";
					PreparedStatement ps = con.prepareStatement( query );
					ps.setString( 1, tableName );
					ResultSet rs = ps.executeQuery();
					if( rs.next() ) result = true;
					rs.close();
					ps.close();
				}

				if( result ) periodicTableSet.add( tableName );
			}
		}
		catch( Exception ex )
		{
			log.error( ex.getMessage() );
		}

		return result;
	}
}
