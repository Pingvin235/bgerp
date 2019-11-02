package ru.bgcrm.cache;

import java.sql.Connection;

import org.apache.log4j.Logger;

import ru.bgcrm.util.Setup;
import ru.bgcrm.util.sql.SQLUtils;

//TODO: Пока поиск сделан в БД, возможно не понадобится ещё.
public class AddressCache
	extends Cache<AddressCache>
{
	private static Logger log = Logger.getLogger( AddressCache.class );
	
	private static CacheHolder<AddressCache> holder = new CacheHolder<AddressCache>( new AddressCache() );

	public static void flush( Connection con )
	{
		holder.flush( con );
	}
	
	// конец статической части
	
	// полный перечень улиц
	@Override
	protected AddressCache newInstance()
    {
		AddressCache result = new AddressCache();
		
		Connection con = Setup.getSetup().getDBConnectionFromPool();
		try
        {
	        
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
