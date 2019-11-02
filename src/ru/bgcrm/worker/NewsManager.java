package ru.bgcrm.worker;

import static ru.bgcrm.dao.user.Tables.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import ru.bgcrm.cache.UserNewsCache;
import ru.bgcrm.dao.Tables;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.sql.SQLUtils;

public class NewsManager	
	implements Runnable
{
	private static final Logger log = Logger.getLogger( NewsManager.class );
	
	@Override
	public void run()
	{	
		Connection con = Setup.getSetup().getDBConnectionFromPool();
		
		try
		{ 
			//Обновление новостей: пометка прочитанными
			String query = " UPDATE " + Tables.TABLE_NEWS_USER + " u " +
						   " INNER JOIN " + Tables.TABLE_NEWS + " n ON u.news_id=n.id SET u.is_read=1 " +
						   " WHERE DATE_ADD(n.create_dt, INTERVAL n.read_time HOUR) < NOW() AND u.is_read=0 ";
			PreparedStatement ps = con.prepareStatement( query );			
			ps.executeUpdate();
			ps.close();
			con.commit();
						
			//Обновление новостей: удаление отживших свое
			query = " DELETE FROM " + Tables.TABLE_NEWS + " WHERE DATE_ADD(create_dt, INTERVAL life_time DAY) < NOW() ";
			ps = con.prepareStatement( query );			
			ps.executeUpdate();
			ps.close();
			con.commit();
			
			//Обновление новостей: удаление связок новость-пользователь, где новость указывает на несуществующую
			query = " DELETE u.* FROM " + Tables.TABLE_NEWS_USER + " u " +
					" LEFT JOIN " + Tables.TABLE_NEWS + " n ON u.news_id=n.id " +
					" WHERE n.id IS NULL ";			
			ps = con.prepareStatement( query );			
			ps.executeUpdate();
			ps.close();
			con.commit();
			
			//Обновление новостей: удаление связок новость-пользователь, где пользователь указывает на несуществующего или удаленного
			query = " DELETE nu.* FROM " + Tables.TABLE_NEWS_USER + " nu " + 
					" LEFT JOIN " + TABLE_USER + " u ON nu.user_id=u.id " +
					" WHERE u.id IS NULL OR u.deleted=1 ";
						
			ps = con.prepareStatement( query );			
			ps.executeUpdate();
			ps.close();
			con.commit();
		}
		catch( SQLException e )		
		{
			log.error( e.getMessage(), e );
		}
		finally 
		{
			UserNewsCache.flush( con );
			SQLUtils.closeConnection( con );			
		}
	}
}
