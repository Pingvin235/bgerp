package ru.bgcrm.cache;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import ru.bgcrm.dao.process.QueueDAO;
import ru.bgcrm.model.process.Queue;
import ru.bgcrm.model.user.User;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.SQLUtils;

public class ProcessQueueCache
	extends Cache<ProcessQueueCache>
{
	private static Logger log = Logger.getLogger( ProcessQueueCache.class );
	
	private static CacheHolder<ProcessQueueCache> holder = new CacheHolder<ProcessQueueCache>( new ProcessQueueCache() );

	public static Queue getQueue( int id, User user )
	{
		Queue result = holder.getInstance().queueMap.get( id );
		// фильтр по разрешённым очередям процессов
		if( result != null && user != null && !user.getQueueIds().contains( result.getId() ) )
		{
			result = null;
		}
		return result;
	}

	public static Queue getQueue( int id )
	{
		return holder.getInstance().queueMap.get( id );
	}

	public static Map<Integer, Queue> getQueueMap()
	{
		return holder.getInstance().queueMap;
	}

	public static List<Queue> getQueueList()
	{
		return holder.getInstance().queueList;
	}

	public static List<Queue> getUserQueueList( User user )
	{
		List<Queue> result = new ArrayList<Queue>();

		for( Queue queue : holder.getInstance().queueList )
		{
			if( user.getQueueIds().contains( queue.getId() ) )
			{
				result.add( queue );
			}
		}

		return result;
	}

	public static void flush( Connection con )
	{
		holder.flush( con );
	}

	// конец статической части

	private Map<Integer, Queue> queueMap;
	private List<Queue> queueList;

	@Override
	protected ProcessQueueCache newInstance()
	{
		ProcessQueueCache result = new ProcessQueueCache();

		Connection con = Setup.getSetup().getDBConnectionFromPool();
		try
		{
			result.queueMap = new HashMap<Integer, Queue>();
			result.queueList = new ArrayList<Queue>();

			QueueDAO queueDAO = new QueueDAO( con );
			for( Queue queue : queueDAO.getQueueList() )
			{
				// выбор явно указанных в конфигурации очереди типов процессов
				queue.setProcessTypeIds( queueDAO.getQueueProcessTypeIds( queue.getId() ) );

				if( log.isDebugEnabled() )
				{
					log.debug( "Queue " + queue.getId() + " selected process types: " + Utils.toString( queue.getProcessTypeIds() ) );
				}

				// выбор дочерних типов привязанных процессов
				queue.setProcessTypeIds( ProcessTypeCache.getTypeTreeRoot().getSelectedChildIds( queue.getProcessTypeIds() ) );

				if( log.isDebugEnabled() )
				{
					log.debug( "Queue " + queue.getId() + " process types with childs: " + Utils.toString( queue.getProcessTypeIds() ) );
				}

				queue.extractFiltersAndSorts();

				result.queueMap.put( queue.getId(), queue );
				result.queueList.add( queue );
			}
		}
		catch( Exception e )
		{
			log.error( e.getMessage(), e );
		}
		finally
		{
			SQLUtils.closeConnection( con );
		}

		return result;
	}
}
