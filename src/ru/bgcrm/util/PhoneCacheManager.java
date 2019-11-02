package ru.bgcrm.util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import ru.bgcrm.model.PhoneCacheItem;

public class PhoneCacheManager
{
    private int cacheSize = 300;
    private Map<String, PhoneCacheItem> cacheMap = new HashMap<String, PhoneCacheItem>();
    private static PhoneCacheManager cacheManager; 

    private PhoneCacheManager()
    {
    }
    
    public static PhoneCacheManager getCacheManager()
    {
        if ( cacheManager == null )
        {
            cacheManager = new PhoneCacheManager();
        }
        return cacheManager;
    }
    
    public PhoneCacheItem getItem( String key )
    {
        PhoneCacheItem cacheItem = null;
        synchronized( cacheMap )
        {
            cacheItem = cacheMap.get( key );
            if ( cacheItem != null )
            {
                cacheItem.setLastTimeAccess( new Date() );
            }
        }
        return cacheItem;
    }

    public void putItem( PhoneCacheItem cacheItem )
    {
        synchronized( cacheMap )
        {
            while ( cacheMap.size() > cacheSize )
            {
                Date date = new Date();
                PhoneCacheItem cacheItemForDelete = null;
                for ( PhoneCacheItem item : cacheMap.values() )
                {
                    if ( date.after( item.getLastTimeAccess() ) )
                    {
                        date = item.getLastTimeAccess();
                        cacheItemForDelete = item;
                    }
                }
                if ( cacheItemForDelete != null )
                {
                    cacheMap.remove( cacheItemForDelete.getKey() );
                }
            }
            cacheItem.setLastTimeAccess( new Date() );
            cacheMap.put( cacheItem.getKey(), cacheItem );
        }
    }

    public int getCacheSize()
    {
        return cacheSize;
    }

    public int getCountItemInCache()
    {
        return cacheMap.size();
    }
    
    public void setCacheSize( int cacheSize )
    {
        this.cacheSize = cacheSize;
    }
}
