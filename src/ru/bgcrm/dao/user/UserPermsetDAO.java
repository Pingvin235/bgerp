package ru.bgcrm.dao.user;

import static ru.bgcrm.dao.user.Tables.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import ru.bgcrm.model.BGException;
import ru.bgcrm.model.Page;
import ru.bgcrm.model.SearchResult;
import ru.bgcrm.model.user.Permset;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Preferences;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.PreparedDelay;

public class UserPermsetDAO
    extends UserDAO
{
	public UserPermsetDAO( Connection con )
	{
		super( con );
	}
	
	public void searchPermset( SearchResult<Permset> searchResult )
        throws BGException
    {
    	searchPermset( searchResult, null );
    }

	public void searchPermset( SearchResult<Permset> searchResult, String filterLike )
	    throws BGException
	{
		Page page = searchResult.getPage();
		try
		{
			List<Permset> list = searchResult.getList();
			
			PreparedDelay pd = new PreparedDelay( con );
			pd.addQuery( SQL_SELECT_COUNT_ROWS + " p.*, pp.permset_id FROM " + TABLE_USER_PERMSET_TITLE + " AS p " );
			pd.addQuery( "LEFT JOIN " + TABLE_PERMSET_PERMISSION + " AS pp ON p.id=pp.permset_id " );
			if( Utils.notBlankString( filterLike ) )
			{
				pd.addQuery( "AND pp.config LIKE ? " );
				pd.addString( filterLike );
			}
			
			pd.addQuery( " GROUP BY p.id " );
		
			if( Utils.notBlankString( filterLike ) )
			{
				pd.addQuery( "HAVING title LIKE ? OR comment LIKE ? OR config LIKE ? OR pp.permset_id>0");
				pd.addString( filterLike );
				pd.addString( filterLike );
				pd.addString( filterLike );
			}			
			pd.addQuery( " ORDER BY p.title" );
			pd.addQuery( getMySQLLimit( page ) );
						
			ResultSet rs = pd.executeQuery();
			while( rs.next() )
			{
				list.add( getFromRS( rs ) );
			}
			
			if( page != null )
			{
				page.setRecordCount( getFoundRows( pd.getPrepared() ) );
			}
			pd.close();
		}
		catch( SQLException e )
		{
			throw new BGException( e );
		}
	}

	public List<Permset> getPermsetList()
	    throws BGException
	{
		List<Permset> result = new ArrayList<Permset>();
		
		try
		{
			String query =  "SELECT * FROM " + TABLE_USER_PERMSET_TITLE + " ORDER BY title";
			PreparedStatement ps = con.prepareStatement( query );
			
			ResultSet rs = ps.executeQuery();
			while( rs.next() )
			{
				result.add( getFromRS( rs ) );
			}
			ps.close();
		}
		catch( SQLException e )
		{
			throw new BGException( e );
		}
		
		return result;
	}
	
	public Permset getPermsetById( int id )
		throws BGException
	{
		Permset result = null;
		
		try
		{
			String query = "SELECT * FROM " + TABLE_USER_PERMSET_TITLE + " WHERE id=?";
			PreparedStatement ps = con.prepareStatement( query );
			ps.setInt( 1, id );
			
			ResultSet rs = ps.executeQuery();
			if( rs.next() )
			{
				result = getFromRS( rs );
			}
			ps.close();
		}
		catch( SQLException e )
		{
			throw new BGException( e );
		}
		
		return result;
	}
	
	public void deletePermset( int id )
		throws BGException
	{
		deleteById( TABLE_USER_PERMSET_TITLE, id );
	}
	
	private Permset getFromRS( ResultSet rs )
	    throws SQLException
	{
		Permset result = new Permset();

		result.setId( rs.getInt( "id" ) );
		result.setTitle( rs.getString( "title" ) );
		result.setRoles( rs.getString( "roles" ) );
		result.setComment( rs.getString( "comment" ) );
		result.setConfig( rs.getString( "config" ) );

		return result;
	}

	public void updatePermset( Permset userGroup )
		throws BGException
	{
		int index = 1;
		PreparedStatement ps;

		try
		{
			if( userGroup.getId() <= 0 )
			{
				ps = con.prepareStatement( "INSERT INTO " + TABLE_USER_PERMSET_TITLE + " (title, roles, comment) VALUES (?,?,?)",
										   PreparedStatement.RETURN_GENERATED_KEYS );
				ps.setString( index++, userGroup.getTitle() );
				ps.setString( index++, userGroup.getRoles() );
				ps.setString( index++, userGroup.getComment() );
				ps.executeUpdate();
				userGroup.setId( lastInsertId( ps ) );
			}
			else
			{
				ps = con.prepareStatement( "UPDATE " + TABLE_USER_PERMSET_TITLE + " SET title=?,  roles=?, comment=?, config=? WHERE id=?" );
				ps.setString( index++, userGroup.getTitle() );
				ps.setString( index++, userGroup.getRoles() );
				ps.setString( index++, userGroup.getComment() );
				ps.setString( index++, userGroup.getConfig() );
				ps.setInt( index++, userGroup.getId() );

				ps.executeUpdate();
			}

			ps.close();
		}
		catch( SQLException e )
		{
			throw new BGException( e );
		}
	}

	public Map<Integer, Map<String, ParameterMap>> getAllPermsets()
	    throws BGException
	{
		return getAllPermissions( Tables.TABLE_PERMSET_PERMISSION, "permset_id" );
	}
	
	public void updatePermissions( Set<String> action, Set<String> config, int permsetId )
    	throws BGException
    {
    	try
    	{
    		String query = "DELETE FROM " + TABLE_PERMSET_PERMISSION + " WHERE permset_id=?";
    		
			PreparedStatement ps = con.prepareStatement( query );
    		ps.setInt( 1, permsetId );
    		ps.executeUpdate();
    		ps.close();
    
    		for( String newAction : action )
    		{
    			String newConfig = "";
    			for( String c : config )
    			{
    				if( c.startsWith( newAction + "#" ) )
    				{
    					newConfig = StringUtils.substringAfter( c, newAction + "#" );
    					break;
    				}
    			}
    
    			query =  
    				"INSERT INTO " + TABLE_PERMSET_PERMISSION + " ( permset_id, action, config ) " +
    				"VALUES ( ? , ? , ? )";
    			
    			ps = con.prepareStatement( query );
    			ps.setInt( 1, permsetId );
    			ps.setString( 2, newAction );
    			ps.setString( 3, newConfig );
    			ps.executeUpdate();
    			ps.close();
    		}    
    	}
    	catch( SQLException e )
    	{
    		throw new BGException();
    	}
    }
    
    public Map<String, ParameterMap> getPermissions( int permsetId )
    	throws BGException
    {
    	try
    	{
    		String query = "SELECT * FROM " + TABLE_PERMSET_PERMISSION + " WHERE permset_id=?";
    		
			PreparedStatement ps = con.prepareStatement( query );
    		ps.setInt( 1, permsetId );
    
    		Map<String, ParameterMap> perms = new HashMap<String, ParameterMap>();
    
    		ResultSet rs = ps.executeQuery();
    		while( rs.next() )
    		{
    			String action = rs.getString( "action" );
    			String config = rs.getString( "config" );
    
    			perms.put( action, new Preferences( config ) );
    		}
    		ps.close();
    
    		return perms;
    	}
    	catch( SQLException e )
    	{
    		throw new BGException();
    	}
    }
    
    public void replacePermissions( int fromPermsetId, int toPermsetId )
    	throws BGException
    {
    	try
		{
			String query = "DELETE FROM " + TABLE_PERMSET_PERMISSION + " WHERE permset_id=?";
			
			PreparedStatement ps = con.prepareStatement( query );
			ps.setInt( 1, toPermsetId );
			ps.executeUpdate();
			ps.close();
			
			query = 
				"INSERT INTO " + TABLE_PERMSET_PERMISSION + " (permset_id, action, config) " +
				"SELECT ?, action, config FROM " + TABLE_PERMSET_PERMISSION + 
				"WHERE permset_id=?";
			
			ps = con.prepareStatement( query );
			ps.setInt( 1, toPermsetId );
			ps.setInt( 2, fromPermsetId );
			ps.executeUpdate();
			ps.close();
		}
		catch( SQLException e )
		{
			throw new BGException( e );
		}
    }
}