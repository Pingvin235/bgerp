package ru.bgcrm.util;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import ru.bgcrm.util.sql.SQLUtils;

public class StatData
{
	private static StatData statData;
	private Map<String, BigDecimal> statDataMap = new HashMap<String, BigDecimal>();
	private Date lastUpdate = new Date();
	
	private StatData()
	{}
	
	public static StatData getStatData()
	{
		if ( statData == null )
		{
			statData = new StatData();
		}
		return statData;
	}

	public Map<String, BigDecimal> getStatDataMap()
    {
		Calendar date1 = new GregorianCalendar();
		date1.setTime( lastUpdate );
		Calendar date2 = new GregorianCalendar();
		date2.add( Calendar.MINUTE, -10 );
		if ( date1.before( date2 ) )
		{
			Connection con = Setup.getSetup().getDBConnectionFromPool();
			try
			{
				setData( con, "SELECT count(*) FROM customer", "customer" );
				setData( con, "SELECT count(*) FROM customer WHERE date_created>DATE_SUB(NOW(), INTERVAL 24 HOUR)", "customer24" );
				setData( con, "SELECT count(*) FROM param_address WHERE value='address'", "param_address_bad" );
				setData( con, "SELECT count(*) FROM param_address", "param_address" );
				setData( con, "SELECT count(*) FROM param_phone_item", "param_phone_item" );
				setData( con, "SELECT count(*) FROM param_phone", "param_phone" );
				setData( con, "SELECT count(*) FROM customer_contract_link", "contract" );
				lastUpdate = new Date();
			}
			catch( SQLException ex )
			{
				ex.printStackTrace();
			}
			finally
			{
				SQLUtils.closeConnection( con );
			}
		}
    	return statDataMap;
    }

	private void setData( Connection con, String query, String key )
	    throws SQLException
	{
		ResultSet rs = null;
		rs = con.createStatement().executeQuery( query );
		while( rs.next() )
		{
			statDataMap.put( key, rs.getBigDecimal( 1 ) );
		}
		rs.close();
	}
}