package ru.bgcrm.model.work.config;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import ru.bgcrm.model.work.WorkDaysCalendar;
import ru.bgcrm.model.work.WorkDaysCalendarRule;
import ru.bgcrm.util.Config;
import ru.bgcrm.util.ParameterMap;

public class CalendarConfig
	extends Config
{
	private final Map<Integer, WorkDaysCalendar> calendarMap = new LinkedHashMap<Integer, WorkDaysCalendar>();
	
	public CalendarConfig( ParameterMap setup )
	{
		super( setup );
		
		for( Entry<Integer, ParameterMap> me : setup.subIndexed( "callboard.workdays.calendar." ).entrySet() )
		{
			int id = me.getKey();
			ParameterMap config = me.getValue();
			
			calendarMap.put( id, new WorkDaysCalendar( id, config.get( "title", "" ), config.get( "comment", "" ), 
			                                           WorkDaysCalendarRule.createFromString( config.get( "rule", "" ) ) ) );
		}
	}

	public Collection<WorkDaysCalendar> getCalendars()
	{
		return calendarMap.values();
	}
	
	public WorkDaysCalendar getCalendar( int id )
	{
		return calendarMap.get( id );
	}
}
