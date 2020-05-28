package ru.bgcrm.model.work.config;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ru.bgcrm.util.Config;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.TimeUtils;

public class CallboardPlanConfig
	extends Config
{
	// минуты от начала дня - начало плана
	private final int dayMinuteFrom;
	// минуты от начала дня - окончание плана
	private final int dayMinuteTo;
	// шаг времени в минутах
	private final int dayMinuteStep;
	
	public CallboardPlanConfig( ParameterMap setup )
	{
		super( setup );
		
		this.dayMinuteFrom = setup.getInt( "dayMinuteFrom", 0 );
		this.dayMinuteTo = setup.getInt( "dayMinuteTo", 0 );
		this.dayMinuteStep = setup.getInt( "dayMinuteStep", 60 );
	}

	public int getDayMinuteFrom()
	{
		return dayMinuteFrom;
	}

	public int getDayMinuteTo()
	{
		return dayMinuteTo;
	}

	public int getDayMinuteStep()
	{
		return dayMinuteStep;
	}
	
	public Calendar getTimeFrom( Date date )
	{
		Calendar result = TimeUtils.convertDateToCalendar( date );
		result.add( Calendar.MINUTE, dayMinuteFrom );
		return result;
	}
	
	public Calendar getTimeTo( Date date )
	{
		Calendar result = TimeUtils.convertDateToCalendar( date );
		result.add( Calendar.MINUTE, dayMinuteTo );
		return result;
	}
	
	public List<Date> getDateTimes( Date date )
	{
		List<Date> result = new ArrayList<Date>();
		
		for( int minute = dayMinuteFrom; minute < dayMinuteTo; minute += dayMinuteStep )
		{
			Calendar time = TimeUtils.convertDateToCalendar( date );
			time.add( Calendar.MINUTE, minute );
			result.add( TimeUtils.convertCalendarToDate( time ) );
		}
		
		return result;
	}
}