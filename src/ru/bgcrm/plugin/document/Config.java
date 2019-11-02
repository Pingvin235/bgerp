package ru.bgcrm.plugin.document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import ru.bgcrm.plugin.document.model.Pattern;
import ru.bgcrm.util.ParameterMap;

public class Config 
	extends ru.bgcrm.util.Config
{
	private Map<String, SortedMap<Integer, Pattern>> patterns = new HashMap<String, SortedMap<Integer,Pattern>>();
	
	public Config( ParameterMap setup )
    {
		super( setup );
		for( Map.Entry<Integer, ParameterMap> me : setup.subIndexed( "document:pattern.", "patterndoc." ).entrySet() )
		{
			int id = me.getKey();
			ParameterMap params = me.getValue();
			
			try
            {
				Pattern pattern = new Pattern( id, params );
				
				SortedMap<Integer, Pattern> map = patterns.get( pattern.getScope() );
				if( map == null )
				{
					map = new TreeMap<Integer, Pattern>();
					patterns.put( pattern.getScope(), map );
				}
				map.put( pattern.getId(), pattern );
            }
            catch( Exception e )
            {
	            log.error( e.getMessage(), e );
            }
		}
    }
	
	public List<Pattern> getPatterns( String scope, String objectType, String objectTitle )
	{
		List<Pattern> result = new ArrayList<Pattern>();
		
		SortedMap<Integer, Pattern> patterns = this.patterns.get( scope );
		if( patterns != null )
		{
			for( Pattern pattern : patterns.values() )
			{
				if( pattern.checkTitle( objectTitle ) && pattern.checkType( objectType ))
				{
					result.add( pattern );
				}
			}
		}
		
		return result;
	}
	
	public Pattern getPattern( String scope, int id )
	{
		SortedMap<Integer, Pattern> patterns = this.patterns.get( scope );
		if( patterns != null )
		{
			return patterns.get( id );
		}
		return null;	
	}
}
