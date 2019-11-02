package ru.bgcrm.cache;

import java.sql.Connection;
import java.util.Map;

import org.apache.log4j.Logger;

import ru.bgcrm.dao.work.WorkTypeDAO;
import ru.bgcrm.model.work.WorkType;
import ru.bgcrm.util.Setup;

public class CallboardCache
	extends Cache<CallboardCache>
{
	private static Logger log = Logger.getLogger( CallboardCache.class );
	
	private static CacheHolder<CallboardCache> holder = new CacheHolder<CallboardCache>( new CallboardCache() );
	
	public static WorkType getWorkType( int id )
	{
		return holder.getInstance().workTypeMap.get( id );
	}
	
	public static Map<Integer, WorkType> getWorkTypeMap()
	{
		return holder.getInstance().workTypeMap;
	}
	
	public static void flush( Connection con )
	{
		holder.flush( con );
	}
	
	// конец статической части 

	private Map<Integer, WorkType> workTypeMap;
		
	@Override
	protected CallboardCache newInstance()
	{
		CallboardCache result = new CallboardCache();

		Connection con = Setup.getSetup().getDBConnectionFromPool();
		try
		{
			WorkTypeDAO typeDao = new WorkTypeDAO( con );
			
			result.workTypeMap = typeDao.getWorkTypeMap();
		}
		catch( Exception ex )
		{
			log.error( ex.getMessage(), ex );
		}
		
		return result;
	}
}