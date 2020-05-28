package ru.bgcrm.model.work;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import ru.bgcrm.model.IdTitle;
import ru.bgcrm.model.Pair;
import ru.bgcrm.model.work.config.DayTypeConfig;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.TimeUtils;

public class WorkDaysCalendar
	extends IdTitle
{
	private String comment = "";
	private List<WorkDaysCalendarRule> rules = new ArrayList<WorkDaysCalendarRule>();
	//private Set<WorkDaysCalendarExclude> excludes = new HashSet<WorkDaysCalendarExclude>();

	public WorkDaysCalendar()
	{}

	public WorkDaysCalendar( int id, String title, String comment, List<WorkDaysCalendarRule> rules )
	{
		super( id, title );

		this.comment = comment;
		this.rules = rules;
	}

	public String getComment()
	{
		return comment;
	}

	public void setComment( String comment )
	{
		this.comment = comment;
	}

/*	public Set<WorkDaysCalendarExclude> getExcludes()
	{
		return excludes;
	}

	public void setExcludes( Set<WorkDaysCalendarExclude> excludes )
	{
		this.excludes = excludes;
	}

	public Set<WorkDaysCalendarRule> getRules()
	{
		return rules;
	}

	public void setRules( Set<WorkDaysCalendarRule> rules )
	{
		this.rules = rules;
	}
	*/
	
	public Pair<DayType, Boolean> getDayType( Date date, Map<Date, Integer> excludeDates )
	{
		DayTypeConfig config = Setup.getSetup().getConfig( DayTypeConfig.class );
		
		Integer result = null;
		
		if( excludeDates != null )
		{
    		result = excludeDates.get( date );
    		if( result != null )
    		{
    			return new Pair<DayType, Boolean>( config.getType( result ), true );
    		}
		}
		
		int dayOfWeek = TimeUtils.getDayOfWeekPosition( date );
		for( WorkDaysCalendarRule rule : rules )
		{
			if( rule.getDay() == dayOfWeek )
			{
				result = rule.getType();
				break;
			}
		}
		
		if( result != null )
		{
			return new Pair<DayType, Boolean>( config.getType( result ), false );
		}
		
		return null;
	}

	/*public Map<Integer, Integer> getRulesMap()
	{
		Map<Integer, Integer> result = new HashMap<Integer, Integer>();

		for( WorkDaysCalendarRule rule : rules )
		{
			result.put( rule.getDay(), rule.getType() );
		}

		return result;
	}

	public Set<Date> getExcludesDateSet()
	{
		Set<Date> result = new HashSet<Date>();

		for( WorkDaysCalendarExclude item : excludes )
		{
			result.add( item.getDate() );
		}

		return result;
	}

	public Map<Date, Integer> getExcludesMap()
	{
		Map<Date, Integer> result = new HashMap<Date, Integer>();
		{
			for( WorkDaysCalendarExclude item : excludes )
			{
				result.put( item.getDate(), item.getType() );
			}
		}

		return result;
	}*/
}