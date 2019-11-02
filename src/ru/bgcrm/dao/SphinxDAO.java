package ru.bgcrm.dao;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import ru.bgcrm.model.BGException;
import ru.bgcrm.model.Customer;
import ru.bgcrm.model.Page;
import ru.bgcrm.model.SearchResult;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.sql.SQLUtils;

@Deprecated
public class SphinxDAO
	extends CommonDAO
{
	private Logger logger = Logger.getLogger( SphinxDAO.class );

	private static final String DEFAULT_HOST = "127.0.0.1";
	private static final int DEFAULT_PORT = 9306;
	private static final String DEFAULT_USER = "";
	private static final String DEFAULT_PASS = "";

	private static final int DEFAULT_CACHE_TIMEOUT = 300;
	private static final int DEFAULT_CACHE_LIMIT = 500;

	private static final String INDEX_CUSTOMER_NAME = " customer ";
	private static final String TABLE_CUSTOMER_CACHE = " customer_cache ";

	private static final String STAR = "*";
	private static final String SPACE = " ";

	private static final int CACHED = 1;
	private static final int NOT_CACHED = 0;

	private class Stat
	{
		private int total;
		private int totalFound;
		private int time;
		List<KeywordStat> keyword = new ArrayList<KeywordStat>();

	}

	private class KeywordStat
	{
		private String keyword;
		private int docCount;
		private int hitCount;

		public String getKeyword()
		{
			return keyword;
		}

		public int getDocCount()
		{
			return docCount;
		}

		public int getHitCount()
		{
			return hitCount;
		}

		public KeywordStat( String keyword )
		{
			this.keyword = keyword;
		}
	}

	private final String host;
	private final int port;

	private final int cacheTimeout;
	private final int cacheLimit;

	public SphinxDAO( Connection con )
		throws BGException
	{
		super( con );

		Setup setup = Setup.getSetup();
		ParameterMap conf = setup.sub( "sphinx." );
		this.host = conf.get( "url", DEFAULT_HOST );
		this.port = conf.getInt( "port", DEFAULT_PORT );
		this.cacheTimeout = conf.getInt( "interval", DEFAULT_CACHE_TIMEOUT );
		this.cacheLimit = conf.getInt( "limit", DEFAULT_CACHE_LIMIT );
	}

	private Connection getConnection()
		throws BGException
	{
		try
		{
			try
			{
				@SuppressWarnings("unused")
				Driver driver = (Driver)Class.forName( "com.mysql.jdbc.Driver" ).newInstance();
				return DriverManager.getConnection( "jdbc:mysql://" + host + ":" + port, DEFAULT_USER, DEFAULT_PASS );
			}
			catch( ClassNotFoundException e )
			{
				throw new BGException( e );
			}
			catch( IllegalAccessException e )
			{
				throw new BGException( e );
			}
			catch( InstantiationException e )
			{
				throw new BGException( e );
			}
		}
		catch( SQLException e )
		{
			throw new BGException( e );
		}
	}

	public Stat getStat( PreparedStatement ps )
		throws BGException
	{
		try
		{
			ResultSet rs = ps.executeQuery( " SHOW META " );
			Stat stat = new Stat();
			while( rs.next() )
			{
				int index = 1;
				String param = rs.getString( index++ );
				if( "total".equals( param ) )
				{
					stat.total = rs.getInt( index++ );
				}
				else if( "total_found".equals( param ) )
				{
					stat.totalFound = rs.getInt( index++ );
				}
				else if( "time".equals( param ) )
				{
					stat.time = -1;
				}
				else if( param.startsWith( "keyword" ) )
				{
					stat.keyword.add( new KeywordStat( rs.getString( index++ ) ) );
				}
				else if( param.startsWith( "docs" ) )
				{
					stat.keyword.get( stat.keyword.size() - 1 ).docCount = rs.getInt( index++ );
				}
				else if( param.startsWith( "hits" ) )
				{
					stat.keyword.get( stat.keyword.size() - 1 ).hitCount = rs.getInt( index++ );
				}
			}

			return stat;
		}
		catch( SQLException e )
		{
			throw new BGException( e );
		}
	}

	public void searchCustomer( SearchResult<Customer> searchResult, String searchString )
		throws BGException
	{
		if( searchResult == null )
		{
			return;
		}

		Connection sphinxCon = null;
		try
		{
			sphinxCon = getConnection();
			Page page = searchResult.getPage();
			List<Customer> list = searchResult.getList();

			StringBuilder query = new StringBuilder();
			query.append( SQL_SELECT );
			query.append( " id, title, reference " );
			query.append( SQL_FROM );
			query.append( INDEX_CUSTOMER_NAME );
			query.append( SQL_WHERE );
			query.append( " MATCH( ? )" );
			query.append( getMySQLLimit( page ) );

			int index = 1;
			PreparedStatement ps = sphinxCon.prepareStatement( query.toString() );
			ps.setString( index++, STAR + searchString.replace( SPACE, STAR ) + STAR );

			ResultSet rs = ps.executeQuery();

			while( rs.next() )
			{
				index = 1;
				Customer customer = new Customer();
				customer.setId( rs.getInt( index++ ) );
				customer.setTitle( rs.getString( index++ ) );
				customer.setReference( rs.getString( index++ ) );
				list.add( customer );
			}

			Stat stat = getStat( ps );
			page.setRecordCount( stat.total );
			ps.close();
		}
		catch( SQLException e )
		{
			throw new BGException( e );
		}
		finally
		{
			SQLUtils.closeConnection( sphinxCon );
		}
	}

	/**
	 * Находит контрагентов, которых нет в таблице customer_cache и вставляет их туда.
	 * @throws BGException
	 */
	public void insertUnaccountedCusomers()
		throws BGException
	{
		try
		{
			StringBuilder query = new StringBuilder();
			query.append( SQL_INSERT );
			query.append( TABLE_CUSTOMER_CACHE );
			query.append( " ( " );
			query.append( SQL_SELECT );
			query.append( " c.id, " );
			query.append( " 0, " );
			query.append( " NOW() " );
			query.append( SQL_FROM );
			query.append( Tables.TABLE_CUSTOMER );
			query.append( " AS c " );
			query.append( SQL_LEFT_JOIN );
			query.append( TABLE_CUSTOMER_CACHE );
			query.append( " AS cc ON c.id=cc.customer_id " );
			query.append( SQL_WHERE );
			query.append( " cc.customer_id IS NULL " );
			query.append( " ) " );

			PreparedStatement ps = con.prepareStatement( query.toString() );
			int affectedCount = ps.executeUpdate();
			ps.close();

			logger.debug( affectedCount + " customers added to customer_cache." );
		}
		catch( SQLException e )
		{
			throw new BGException( e );
		}
	}

	/**
	 * Помечает контрагента для обновления в кэше
	 * @param con
	 * @param customerId
	 * @throws BGException 
	 */
	public static void customerCacheUpdate( Connection con, int customerId )
		throws BGException
	{
		try
		{
			StringBuilder query = new StringBuilder();
			query.append( SQL_UPDATE );
			query.append( TABLE_CUSTOMER_CACHE );
			query.append( SQL_SET );
			query.append( " cached = ? " );
			query.append( SQL_WHERE );
			query.append( " customer_id = ? " );
			query.append( SQL_LIMIT );
			query.append( " 1 " );

			int index = 1;
			PreparedStatement ps = con.prepareStatement( query.toString() );
			ps.setInt( index++, NOT_CACHED );
			ps.setInt( index++, customerId );

			if( ps.executeUpdate() == 0 )
			{
				ps.close();

				query.setLength( 0 );
				query.append( SQL_INSERT );
				query.append( TABLE_CUSTOMER_CACHE );
				query.append( " ( customer_id, cached ) VALUES ( ?, ? ) " );

				index = 1;
				ps = con.prepareStatement( query.toString() );
				ps.setInt( index++, customerId );
				ps.setInt( index++, NOT_CACHED );

				ps.executeUpdate();
			}

			ps.close();
		}
		catch( SQLException e )
		{
			throw new BGException( e );
		}
	}

	/**
	 * Добавляет в кэш данные, помеченные как измененные
	 * @return Количество записей, занесенных в кэш
	 * @throws BGException
	 */
	public int cache()
		throws BGException
	{
		try
		{
			List<Integer> customerIds = new ArrayList<Integer>();

			String alias = "cache";
			StringBuilder query = new StringBuilder();
			query.append( SQL_SELECT_COUNT_ROWS );
			query.append( " customer_id " );
			query.append( SQL_FROM );
			query.append( TABLE_CUSTOMER_CACHE );
			query.append( " AS " + alias );
			query.append( SQL_WHERE );
			query.append( " " + alias + ".cached = ? " );
			query.append( SQL_AND );
			query.append( " (UNIX_TIMESTAMP( ? ) - UNIX_TIMESTAMP( " + alias + ".cached_dt )) > ? " );
			query.append( SQL_ORDER_BY );
			query.append( " (UNIX_TIMESTAMP( ? ) - UNIX_TIMESTAMP( " + alias + ".cached_dt )) " );
			query.append( SQL_DESC );
			query.append( SQL_LIMIT );
			query.append( " ? " );

			int index = 1;
			PreparedStatement ps = con.prepareStatement( query.toString() );
			ps.setInt( index++, NOT_CACHED );
			ps.setTimestamp( index++, TimeUtils.convertDateToTimestamp( new Date() ) );
			ps.setInt( index++, cacheTimeout );
			ps.setTimestamp( index++, TimeUtils.convertDateToTimestamp( new Date() ) );
			ps.setInt( index++, cacheLimit );

			ResultSet rs = ps.executeQuery();
			while( rs.next() )
			{
				int customerId = rs.getInt( 1 );
				customerIds.add( customerId );
			}
			ps.close();

			cacheCustomers( customerIds );

			return customerIds.size();
		}
		catch( SQLException e )
		{
			throw new BGException( e );
		}
	}

	private void cacheCustomers( List<Integer> customerIds )
		throws BGException
	{
		Connection sphinxCon = null;
		try
		{
			sphinxCon = getConnection();

			for( int customerId : customerIds )
			{

				CustomerDAO customerDAO = new CustomerDAO( con );
				Customer customer = customerDAO.extractCustomerWithRef( customerId );

				if( customer == null )
				{
					delete( customerId );
					continue;
				}

				String indexString = customer.getTitle() + " " + customer.getReference();

				StringBuilder sb = new StringBuilder();
				sb.append( SQL_REPLACE );
				sb.append( INDEX_CUSTOMER_NAME );
				sb.append( " VALUES ( ?, ?, ?, ? ) " );

				int index = 1;
				PreparedStatement ps = sphinxCon.prepareStatement( sb.toString() );
				ps.setInt( index++, customer.getId() );
				ps.setString( index++, indexString );
				ps.setString( index++, customer.getTitle() );
				ps.setString( index++, customer.getReference() );
				ps.executeUpdate();
				ps.close();

				setCached( customer.getId() );
			}

			SQLUtils.commitConnection( sphinxCon );
			logger.info( customerIds.size() + " customers cached to Sphinx. Max=" + cacheLimit );
		}
		catch( SQLException e )
		{
			throw new BGException( e );
		}
		finally
		{
			SQLUtils.closeConnection( sphinxCon );
		}
	}

	public void delete( int customerId )
		throws BGException
	{
		Connection sphinxCon = null;

		try
		{
			// удаляем из индекса
			sphinxCon = getConnection();
			StringBuilder sb = new StringBuilder();
			sb.append( SQL_DELETE );
			sb.append( INDEX_CUSTOMER_NAME );
			sb.append( SQL_WHERE );
			sb.append( " id = ? " );

			int index = 1;
			PreparedStatement ps = sphinxCon.prepareStatement( sb.toString() );
			ps.setInt( index++, customerId );
			ps.executeUpdate();
			ps.close();

			logger.info( "Customer with ID=" + customerId + " dropped from index!" );

			// удаляем из таблицы кэша
			StringBuilder query = new StringBuilder();
			query.append( SQL_DELETE );
			query.append( TABLE_CUSTOMER_CACHE );
			query.append( SQL_WHERE );
			query.append( " customer_id = ? " );

			ps = con.prepareStatement( query.toString() );
			ps.setInt( 1, customerId );
			ps.executeUpdate();
			ps.close();

			logger.info( "Customer with ID=" + customerId + " dropped from cache table!" );
		}
		catch( SQLException e )
		{
			throw new BGException( e );
		}
		finally
		{
			SQLUtils.closeConnection( sphinxCon );
		}
	}

	private void setCached( int customerId )
		throws SQLException
	{
		StringBuilder query = new StringBuilder();
		query.append( SQL_UPDATE );
		query.append( TABLE_CUSTOMER_CACHE );
		query.append( SQL_SET );
		query.append( " cached = ?, cached_dt = ? " );
		query.append( SQL_WHERE );
		query.append( " customer_id = ? " );
		query.append( SQL_LIMIT );
		query.append( " 1 " );

		int index = 1;
		PreparedStatement ps = con.prepareStatement( query.toString() );
		ps.setInt( index++, CACHED );
		ps.setTimestamp( index++, TimeUtils.convertDateToTimestamp( new Date() ) );
		ps.setInt( index++, customerId );

		ps.executeUpdate();
		ps.close();
	}

	public static void main( String[] args )
		throws BGException, SQLException, InterruptedException
	{
		/*Connection con = Setup.getSetup().getDBConnectionFromPool();
		SphinxDAO sphinxDAO = new SphinxDAO( con );

		while( true )
		{
			sphinxDAO.con = con;
			if( sphinxDAO.cache() == 0 )
			{
				break;
			}
			SQLUtils.commitConnection( con );
			SQLUtils.closeConnection( con );

			con = Setup.getSetup().getDBConnectionFromPool();
		}*/

		/*Connection con = Setup.getSetup().getDBConnectionFromPool();
		SphinxDAO sphinxDAO = new SphinxDAO( con );
		sphinxDAO.cache();

		SQLUtils.commitConnection( con );
		SQLUtils.closeConnection( con );*/

		Connection con = Setup.getSetup().getDBConnectionFromPool();
		SphinxDAO sphinxDAO = new SphinxDAO( con );
		sphinxDAO.delete( 1098058 );
		SQLUtils.commitConnection( con );
		SQLUtils.closeConnection( con );
	}
}
