package ru.bgcrm.model.work.config;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import ru.bgcrm.model.work.DayType;
import ru.bgcrm.util.Config;
import ru.bgcrm.util.ParameterMap;

public class DayTypeConfig
	extends Config
{
	private final Map<Integer, DayType> typeMap = new LinkedHashMap<Integer, DayType>();
	
	public DayTypeConfig( ParameterMap setup )
	{
		super( setup );
		
		for( Map.Entry<Integer, ParameterMap>  me : setup.subIndexed( "callboard.workdays.type." ).entrySet() )
		{
			int id = me.getKey();
			typeMap.put( id, new DayType( id, me.getValue() ) );
		}
	}
	
	public Map<Integer, DayType> getTypeMap()
	{
		return typeMap;
	}

	public Collection<DayType> getTypes()
	{
		return typeMap.values();
	}
	
	public DayType getType( int id )
	{
		return typeMap.get( id );
	}
}
