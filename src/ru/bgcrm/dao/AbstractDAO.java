package ru.bgcrm.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import ru.bgcrm.model.BGException;
import ru.bgcrm.model.Id;

/**
 * DAO для работы с конкретным объектом.	
 */
//TODO: Убрать, заменить на AstractDAO.
public abstract class AbstractDAO<B extends Id>
	extends CommonDAO
{
	protected final String tableName;

	public AbstractDAO( Connection con, String tableName )
	{
		this( con, tableName, null );
	}

	public AbstractDAO( Connection con, String tableName, Date date )
	{
		super( con );
		this.con = con;
		this.tableName = getTableName( tableName, date );
	}

	protected String getTableName( String tableName, Date date )
	{
		return tableName;
	}

	protected void processException( SQLException e )
	    throws BGException
	{
		throw new BGException( e );
	}

	public void update( B b )
	    throws BGException
	{
		try
		{
			// Subject subject = Subject.getSubject(
			// AccessController.getContext() );
			// event beforeUpdate
			updateImpl( b );
			// event afterUpdate
		}
		catch( SQLException e )
		{
			processException( e );
		}
	}

	protected abstract void updateImpl( B b )
	    throws BGException, SQLException;

	public B get( int id )
	    throws BGException
	{
		try
		{
			return getImpl( id );
		}
		catch( SQLException e )
		{
			processException( e );
		}

		return null;
	}

	protected B getImpl( int id )
	    throws BGException, SQLException
	{
		return getById( id );
	}

	protected B getById( int id )
	    throws BGException, SQLException
	{
		PreparedStatement ps = con.prepareStatement( "SELECT * FROM " + tableName + " WHERE id=?" );
		ps.setInt( 1, id );

		ResultSet rs = ps.executeQuery();
		if( rs.next() )
		{
			return getFromRS( rs );
		}

		ps.close();

		return null;
	}

	protected List<B> list( String filter, String orderBy, Object... params )
	    throws BGException
	{
		try
		{
			return listImpl( filter, orderBy, params );
		}
		catch( SQLException e )
		{
			processException( e );
		}

		return Collections.emptyList();
	}

	protected B get( String filter, Object... params )
	    throws BGException
	{
		try
		{
			PreparedStatement ps = con.prepareStatement( "SELECT * FROM " + tableName + " WHERE " + filter );
			for( int i = 0; i < params.length; i++ )
			{
				ps.setObject( i + 1, params[i] );
			}

			ResultSet rs = ps.executeQuery();
			if( rs.next() )
			{
				return getFromRS( rs );
			}

		    ps.close();
		}
		catch( SQLException e )
		{
			processException( e );
		}

		return null;
	}

	protected static final Pattern orderByPattern = Pattern.compile( "\\A[\\w\\s\\d,_-]+\\z" );

	protected List<B> listImpl( String filter, String orderBy, Object... params )
	    throws BGException, SQLException
	{
		List<B> result = new ArrayList<B>();

		StringBuilder sb = new StringBuilder().append( "SELECT SQL_CALC_FOUND_ROWS * FROM " ).append( tableName );

		if( filter != null && filter.length() > 0 )
		{
			sb.append( " WHERE " ).append( filter );
		}

		if( orderBy != null && orderBy.length() > 0 )
		{
			if( !orderByPattern.matcher( orderBy ).matches() )
			{
				throw new BGException( "Ошибка запроса" );
			}

			sb.append( " ORDER BY " ).append( orderBy );
		}
		
		String s = sb.toString();

		PreparedStatement ps = con.prepareStatement( sb.toString() );
		for( int i = 0; i < params.length; i++ )
		{
			ps.setObject( i + 1, params[i] );
		}

		ResultSet rs = ps.executeQuery();
		while( rs.next() )
		{
			result.add( getFromRS( rs ) );
		}
		ps.close();

		return result;
	}

	protected abstract B getFromRS( ResultSet rs )
	    throws SQLException;

	protected void getListFromRS( ResultSet rs, List<B> result )
	    throws SQLException
	{
		while( rs.next() )
		{
			result.add( getFromRS( rs ) );
		}
	}

	public int delete( int id )
	    throws BGException
	{
		try
		{
			return deleteImpl( id );
		}
		catch( SQLException e )
		{
			throw new BGException( e );
		}
	}

	protected int deleteImpl( int id )
	    throws BGException, SQLException
	{
		return deleteById( id );
	}

	protected int deleteById( int id )
	    throws SQLException
	{
		PreparedStatement ps = con.prepareStatement( "DELETE FROM " + tableName + " WHERE id=?" );
		ps.setInt( 1, id );

		int result = ps.executeUpdate();
		ps.close();

		return result;
	}
}