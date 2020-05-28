package ru.bgcrm.model.work;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import ru.bgcrm.cache.CallboardCache;
import ru.bgcrm.model.BGException;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Preferences;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;

public class WorkTypeTime
{
	private static final Logger log = Logger.getLogger( WorkTypeTime.class );
	
	private boolean isDynamic;
	private int workTypeId;
	// минуты суток, когда начинается и оканчивается вид работ
	private int dayMinuteFrom;
	private int dayMinuteTo;
	// вид работ целиком располагается в следующих сутках
	private boolean inNextDay;
	private String comment = "";
	
	public WorkTypeTime()
	{}
	
	public WorkTypeTime( boolean isDynamic, int workTypeId, int timeFrom, int timeTo, String comment )
	{
		this.workTypeId = workTypeId;
		this.dayMinuteFrom = timeFrom;
		this.dayMinuteTo = timeTo;
		this.comment = comment;
		this.isDynamic = isDynamic;
	}

	public String getComment()
	{
		return comment;
	}

	public void setComment( String comment )
	{
		this.comment = comment;
	}

	public int getWorkTypeId()
	{
		return workTypeId;
	}

	public void setWorkTypeId( int workTypeId )
	{
		this.workTypeId = workTypeId;
	}

	public int getDayMinuteFrom()
	{
		return dayMinuteFrom;
	}

	public void setDayMinuteFrom( int timeFrom )
	{
		this.dayMinuteFrom = timeFrom;
	}

	public int getDayMinuteTo()
	{
		return dayMinuteTo;
	}

	public void setDayMinuteTo( int timeTo )
	{
		this.dayMinuteTo = timeTo;
	}

	/*public int getMinutesFrom()
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTime( this.timeFrom );

		return calendar.get( Calendar.HOUR_OF_DAY ) * 60 + calendar.get( Calendar.MINUTE );
	}

	public int getMinutesTo()
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTime( this.timeTo );

		return calendar.get( Calendar.HOUR_OF_DAY ) * 60 + calendar.get( Calendar.MINUTE );
	}*/

	public static final String minutesToHourMin( int minutes )
	{
		return String.format( "%02d:%02d", minutes / 60, minutes % 60 );
	}
	
	public String getFormatedTimeFrom()
	{
		return minutesToHourMin( dayMinuteFrom );
	}

	public String getFormatedTimeTo()
	{
		return minutesToHourMin( dayMinuteTo );
	}
	
	public boolean isDynamic()
	{
		return isDynamic;
	}
	
	public boolean getIsDynamic()
	{
		return isDynamic;
	}

	public void setDynamic( boolean isDynamic )
	{
		this.isDynamic = isDynamic;
	}
	
	public static final List<WorkTypeTime> createFromString( String config )
	{
		List<WorkTypeTime> result = new ArrayList<WorkTypeTime>();
		Preferences setup = new Preferences( config );
		
		Map<Integer, ParameterMap> sortedMap = setup.subIndexed( "rule." );
		for( ParameterMap entry : sortedMap.values() )
		{
			try
			{
				WorkTypeTime workTypeTime = new WorkTypeTime();
				workTypeTime.setWorkTypeId( entry.getInt( "workTypeId", 0 ) );
				workTypeTime.setDayMinuteFrom( getMinutes( entry.get( "timeFrom", "" ) ) );
				workTypeTime.setDayMinuteTo( getMinutes( entry.get( "timeTo", "" ) ) );
				workTypeTime.setDynamic( entry.getInt( "isDynamic", 0 ) == 1 );
				
				result.add( workTypeTime );
			}
			catch( BGException e )
			{
				log.error( e.getMessage(), e );
			}
		}
		
		setNextDays( result );

		return result;
	}
	
	public static final void setNextDays( List<WorkTypeTime> workTimes )
	{
		boolean inNextDay = false;
		
		for( WorkTypeTime workTypeTime : workTimes  )
		{
			workTypeTime.inNextDay = inNextDay;
			
			if( !inNextDay &&
				workTypeTime.dayMinuteTo < workTypeTime.dayMinuteFrom )
			{
				inNextDay = true;
			}
		}		
	}
	
	private static final Pattern OLD_TIME_PATTERN = Pattern.compile( "\\d{2}/\\d{2}/\\d{4}\\s+(\\d{2}):(\\d{2})" );
	
	private static final int getMinutes( String value )
		throws BGException
	{
		int result = Utils.parseInt( value, -1 );
		
		// старый формат, использованный при сохранении
		if( result < 0 )
		{
			Matcher m = OLD_TIME_PATTERN.matcher( value );
			if( m.matches() )
			{
				result = Utils.parseInt( m.group( 1 ) ) * 60 + Utils.parseInt( m.group( 2 ) );
			}
			else
			{
				throw new BGException( "Can't parse: " + value );
			}
		}
		
		return result;
	}

	/*
	public static final List<WorkTypeTime> createFromList( List<String> list )
	{
		List<WorkTypeTime> result = new ArrayList<WorkTypeTime>();

		for( String item : list )
		{
			List<String> paramsList = Utils.toList( item, ":" );
			
			if( paramsList.size() == 3 )
			{
				result.add( createFromParams( Integer.parseInt( paramsList.get( 0 ) ), Integer.parseInt( paramsList.get( 1 ) ), Integer.parseInt( paramsList.get( 2 ) ) ) );
			}
			else if( paramsList.size() == 4 )
			{
				result.add( createFromParams( Integer.parseInt( paramsList.get( 0 ) ), Integer.parseInt( paramsList.get( 1 ) ), Integer.parseInt( paramsList.get( 2 ) ), paramsList.get( 3 ) ) );
			}
		}

		return result;
	}
	*/

	/*public static final WorkTypeTime createFromParams( int workTypeId, int timeFrom, int timeTo )
	{
		WorkTypeTime workTypeTime = new WorkTypeTime();
		
		workTypeTime.setWorkTypeId( workTypeId );
		workTypeTime.setDayMinuteFrom( timeFrom );
		workTypeTime.setDayMinuteTo( timeTo );

		return workTypeTime;
	}
	
	public static final WorkTypeTime createFromParams( int workTypeId, int timeFrom, int timeTo, String comment )
	{
		WorkTypeTime workTypeTime = createFromParams( workTypeId, timeFrom, timeTo );
		workTypeTime.setComment( comment );
		
		return workTypeTime;
	}
	
	
	public static final WorkTypeTime createFromParams( boolean isDynamic, int workTypeId, int timeFrom, int timeTo, String comment )
	{
		WorkTypeTime workTypeTime = createFromParams( workTypeId, timeFrom, timeTo, comment );
		workTypeTime.setDynamic( isDynamic );
		
		return workTypeTime;
	}*/

	/*public double getDurationInHours()
	{
		Calendar calendar = null;

		if( timeFrom.after( timeTo ) )
		{
			calendar = Calendar.getInstance();
			calendar.setTime( timeTo );
			calendar.add( Calendar.DAY_OF_YEAR, 1 );
		}

		return hourDelta( timeFrom.getTime(), calendar == null ? timeTo.getTime() : calendar.getTime().getTime() );
	}*/

	/*
	 * Возвращает количество часов, которые переносятся на след. сутки
	 * @return int часы
	 
	public int getMovingHours()
	{
		if( getMinutesFrom() > getMinutesTo() )
		{
			return (int)Math.round( (double)getMinutesTo() / 60 );
		}

		return 0;
	}

	public double hourDelta( long timeFrom, long timeTo )
	{
		long delta = (timeTo - timeFrom) / 1000L;
		return (double)Math.round( ((double)delta / 3600) * 100 ) / 100;
	}
	*/
	
	public int getWorkMinutesInDay( WorkType type, Date dateStart, Date inDate )
	{
		return getMinutesInDay( type, dateStart, inDate, true );
	}

	public int getMinutesInDay( WorkType type, Date dateStart, Date inDate, boolean onlyWork )
	{
		if( type != null && (!onlyWork || !type.isNonWorkHours() ) )
		{
			Calendar inDateStart = TimeUtils.convertDateToCalendar( inDate );
			
    		// все эти сложности вызваны переводами дат
    		Calendar timeFrom = new GregorianCalendar();
    		timeFrom.setTime( dateStart );
    		timeFrom.add( Calendar.MINUTE, dayMinuteFrom );
    		
    		Calendar timeTo = new GregorianCalendar();
    		timeTo.setTime( dateStart );
    		timeTo.add( Calendar.MINUTE, dayMinuteTo );
    		
    		if( dayMinuteTo < dayMinuteFrom )
    		{
    			timeTo.add( Calendar.DAY_OF_YEAR, 1 );
    		}
    		else if( inNextDay )
    		{
    			timeFrom.add( Calendar.DAY_OF_YEAR, 1 );
    			timeTo.add( Calendar.DAY_OF_YEAR, 1 );
    		}
    		
    		// рассматриваются только случаи, что наш период началом или концом
    		// входит в дату, для которой нам нужно получить длительность смены в нём
    		if( inDate != null )
    		{
    			Calendar inDateEnd = TimeUtils.getNextDay( inDateStart );
    			TimeUtils.clear_HOUR_MIN_MIL_SEC( inDateEnd );
    			
    			// нет пересечений
    			if( inDateEnd.before( timeFrom ) || timeTo.before( inDateStart ) )
    			{
    				return 0;
    			}
    			
    			// усечение справа и слева диапазона
    			if( timeFrom.before( inDateStart ) )
    			{
    				timeFrom = inDateStart;    				
    			}
    			else if( inDateEnd.before( timeTo ) )
    			{
    				timeTo = inDateEnd;
    			}    			
    		}
    		
    		// отрицательный резуль
    		return Math.max( (int)((timeTo.getTimeInMillis() - timeFrom.getTimeInMillis()) / 60000L), 0 );
		}
		return 0;
	}
	
	public static int getWorkMinutesInDay( List<WorkTypeTime> workTypeTimeList, Date dateStart, Date inDate )
	{
		int result = 0;
		
		if( workTypeTimeList != null )
    	{
    		for( WorkTypeTime typeTime : workTypeTimeList )
    		{
    			result += typeTime.getWorkMinutesInDay( CallboardCache.getWorkType( typeTime.getWorkTypeId() ), dateStart, inDate );
    		}
    	}
	
		return result;
	}
}