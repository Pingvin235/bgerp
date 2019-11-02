package ru.bgcrm.plugin.phpbb.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ru.bgcrm.model.BGException;
import ru.bgcrm.plugin.phpbb.model.Topic;
import ru.bgcrm.util.TimeUtils;

public class ForumDAO	
{
	private static final String TABLE_NAME =" phpbb_topics ";
	
	private Connection con;
	
	public ForumDAO( Connection con )
	{
		this.con = con;
	}
	
	public Topic getTopic( int id )
    	throws BGException
    {
    	Topic result = null;
    	
    	try
    	{
    		String query = 
    			"SELECT topic_id, topic_title, FROM_UNIXTIME(topic_last_post_time) AS last_post_dt, topic_last_poster_name " +
    			"FROM " + TABLE_NAME + " WHERE topic_id=?";
    		
    		PreparedStatement ps = con.prepareStatement( query );
    		ps.setInt( 1, id );
    		
    		ResultSet rs = ps.executeQuery();
    		if( rs.next() )
    		{
    			result = getTopicFromRs( rs );
    		}
    		ps.close();
    	}
    	catch( SQLException e )
    	{
    		throw new BGException( e );
    	}	
    	
    	return result;
    }
	
	public List<Topic> getTopicListChangedAfter( Date time )
    	throws BGException
    {
    	List<Topic> result = new ArrayList<Topic>();
    	
    	try
    	{
    		String query = 
    			"SELECT topic_id, topic_title, FROM_UNIXTIME(topic_last_post_time) AS last_post_dt, topic_last_poster_name " +
    			"FROM " + TABLE_NAME + " WHERE topic_last_post_time>=UNIX_TIMESTAMP(?)";
    		
    		PreparedStatement ps = con.prepareStatement( query );
    		ps.setTimestamp( 1, TimeUtils.convertDateToTimestamp( time ) );
    		
    		ResultSet rs = ps.executeQuery();
    		while( rs.next() )
    		{
    			result.add( getTopicFromRs( rs ) );
    		}
    		ps.close();
    	}
    	catch( SQLException e )
    	{
    		throw new BGException( e );
    	}	
    	
    	return result;
    }
	
	/*public List<Topic> getTopicList( Set<Integer> idSet )
		throws BGException
	{
		List<Topic> result = new ArrayList<Topic>();
		
		try
		{
			String query = 
				"SELECT topic_id, topic_title, FROM_UNIXTIME(topic_last_post_time) AS last_post_dt " +
				"FROM phpbb_topics WHERE topic_id IN( " + Utils.toString( idSet ) + ")";
			
			PreparedStatement ps = con.prepareStatement( query );
			ResultSet rs = ps.executeQuery();
			while( rs.next() )
			{
				Topic topic = getTopicFromRs( rs );
				
				result.add( topic );
			}
			ps.close();
		}
		catch( SQLException e )
		{
			throw new BGException( e );
		}	
		
		return result;
	}*/
	
	public void updateTopicTitle( int topicId, String title )
		throws BGException
	{
		try
		{
			String query = "UPDATE " + TABLE_NAME + " SET topic_title=? WHERE topic_id=?";
			PreparedStatement ps = con.prepareStatement( query );
			ps.setString( 1, title );
			ps.setInt( 2, topicId );
			ps.executeUpdate();
			ps.close();
		}
		catch( SQLException e )
		{
			throw new BGException( e );
		}
	}

	private Topic getTopicFromRs( ResultSet rs )
		throws SQLException
	{
		Topic topic = new Topic();
		
		topic.setId( rs.getInt( "topic_id" ) );
		topic.setTitle( rs.getString( "topic_title" ) );
		topic.setLastPostTime( TimeUtils.convertTimestampToDate( rs.getTimestamp( "last_post_dt" ) ) );
		topic.setLastPosterName( rs.getString( "topic_last_poster_name" ) );
		
		return topic;
	}
}