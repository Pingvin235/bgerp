package ru.bgcrm.model.customer;

/*
import java.util.Collection;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import ru.bgcrm.util.Config;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Utils;

public class SearchModeConfig
    extends Config
{
	private SortedMap<Integer, SearchMode> modeMap = new TreeMap<Integer, SearchMode>();
	
	public SearchModeConfig( ParameterMap setup )
	{
		super( setup );
		
		for( Map.Entry<Integer, ParameterMap> me : setup.subIndexed( "customer.searchMode." ).entrySet() )
		{
			int id = me.getKey();
			ParameterMap params = me.getValue();
			
			SearchMode searchMode = new SearchMode( id, params.get( "title" ), Utils.toIntegerSet( params.get( "parameterGroupIds" ) ) );
			if( searchMode.getId() > 0 && Utils.notBlankString( searchMode.getTitle() ) )
			{
				modeMap.put( searchMode.getId(), searchMode );
			}
			else
			{
				log.error( "Error load customer search mode " + id );
			}
		}
	}
	
	public Collection<SearchMode> getModes()
	{
		return modeMap.values();
	}
}
*/