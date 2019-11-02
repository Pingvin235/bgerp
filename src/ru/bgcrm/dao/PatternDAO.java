package ru.bgcrm.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import ru.bgcrm.model.BGException;
import ru.bgcrm.model.param.Pattern;

public class PatternDAO
    extends CommonDAO
{
	public PatternDAO( Connection con )
	{
		super( con );
	}

	public List<Pattern> getPatternList( String object )
	    throws SQLException
	{
		List<Pattern> result = new ArrayList<Pattern>();
		ResultSet rs = null;
		PreparedStatement ps = null;
		StringBuilder query = new StringBuilder();
		
		query.append( "SELECT * FROM object_title_pattern WHERE object=? ORDER BY title" );
		ps = con.prepareStatement( query.toString() );
		ps.setString( 1, object );
		rs = ps.executeQuery();
		while( rs.next() )
		{
			Pattern pattern = new Pattern();
			setPatternData( pattern, rs );
			result.add( pattern );
		}
		rs.close();
		ps.close();

		return result;
	}

	public Pattern getPattern( int id )
	    throws SQLException
	{
		Pattern pattern = null;

		String query = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		
		query = "SELECT * FROM object_title_pattern WHERE id=?";
		ps = con.prepareStatement( query );
		ps.setInt( 1, id );
		rs = ps.executeQuery();
		while( rs.next() )
		{
			pattern = new Pattern();
			setPatternData( pattern, rs );
		}
		rs.close();
		ps.close();

		return pattern;
	}

	public void updatePattern( Pattern pattern )
	    throws SQLException
	{

		int index = 1;
		String query = null;
		PreparedStatement ps = null;
		
		if( pattern.getId() <= 0 )
		{
			query = "INSERT INTO object_title_pattern SET object=?, title=?, pattern=?";
			ps = con.prepareStatement( query, PreparedStatement.RETURN_GENERATED_KEYS );
			ps.setString( index++, pattern.getObject() );
			ps.setString( index++, pattern.getTitle() );
			ps.setString( index++, pattern.getPattern() );
			ps.executeUpdate();
			pattern.setId( lastInsertId( ps ) );
		}
		else
		{
			query = "UPDATE object_title_pattern SET title=?, pattern=? WHERE id=?";
			ps = con.prepareStatement( query );
			ps.setString( index++, pattern.getTitle() );
			ps.setString( index++, pattern.getPattern() );
			ps.setInt( index++, pattern.getId() );
			ps.executeUpdate();
		}
		ps.close();
	}
	
	public void deletePattern( int id )
		throws BGException
	{
		try 
		{
			String query = "DELETE FROM object_title_pattern WHERE id=?";
			PreparedStatement ps = con.prepareStatement( query);
			ps.setInt( 1, id );
			ps.executeUpdate();
			ps.close();
		} 
		catch( SQLException e ) 
		{
			throw new BGException( e );
		}	
	}
	
	private void setPatternData( Pattern pattern, ResultSet rs )
	    throws SQLException
	{
		pattern.setId( rs.getInt( "id" ) );
		pattern.setTitle( rs.getString( "title" ) );
		pattern.setPattern( rs.getString( "pattern" ) );
	}
}