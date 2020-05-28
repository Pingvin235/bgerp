package ru.bgcrm.model.work.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

import ru.bgcrm.cache.UserCache;
import ru.bgcrm.model.IdTitle;
import ru.bgcrm.model.user.Group;
import ru.bgcrm.util.Config;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Utils;

public class CallboardConfig
	extends Config
{
	private final Map<Integer, Callboard> itemMap = new LinkedHashMap<Integer, Callboard>();

	public CallboardConfig( ParameterMap setup )
	{
		super( setup );

		for( Entry<Integer, ParameterMap> entry : setup.subIndexed( "callboard." ).entrySet() )
		{
			itemMap.put( entry.getKey(), new Callboard( entry.getKey(), entry.getValue() ) );
		}
	}

	public Callboard get( int id )
	{
		return itemMap.get( id );
	}

	public Collection<Callboard> getCallboards( Set<Integer> allowOnlyIds )
	{
		if( CollectionUtils.isEmpty( allowOnlyIds ) )
		{
			return itemMap.values();
		}
		else
		{
			List<Callboard> result = new ArrayList<Callboard>();
			for( Callboard callboard : itemMap.values() )
			{
				if( allowOnlyIds.contains( callboard.getId() ) )
				{
					result.add( callboard );
				}
			}
			return result;
		}
	}

	public static final class Callboard
		extends IdTitle
	{
		private final ParameterMap configMap;
		private final int groupId;
		private final int calendarId;
		private Boolean hideEmptyGroups = false;
		private Boolean hideEmptyShifts = false;
		private final CallboardTabelConfig tabelConfig;
		private final CallboardPlanConfig planConfig;

		public Callboard( int id, ParameterMap config )
		{
			super( id, config.get( "title" ) );

			this.configMap = config;
			this.groupId = config.getInt( "groupId", 0 );
			this.calendarId = config.getInt( "calendarId", 0 );

			if( Utils.isBlankString( title ) )
			{
				Group group = UserCache.getUserGroup( groupId );
				if( group != null )
				{
					this.title = group.getTitle();
				}
			}

			CallboardTabelConfig tabelConfig = new CallboardTabelConfig( config.sub( "tabel." ) );
			this.tabelConfig = Utils.notBlankString( tabelConfig.getTemplatePath() ) ? tabelConfig : null;

			CallboardPlanConfig planConfig = new CallboardPlanConfig( config.sub( "plan." ) );
			this.planConfig = planConfig.getDayMinuteTo() > 0 ? planConfig : null;
		}

		public int getGroupId()
		{
			return groupId;
		}

		public int getCalendarId()
		{
			return calendarId;
		}

		public ParameterMap getConfigMap()
		{
			return configMap;
		}

		public CallboardTabelConfig getTabelConfig()
		{
			return tabelConfig;
		}

		public CallboardPlanConfig getPlanConfig()
		{
			return planConfig;
		}

		public void setHideEmptyGroups( Boolean hideEmptyGroups )
		{
			this.hideEmptyGroups = hideEmptyGroups;
		}

		public void setHideEmptyShifts( Boolean hideEmptyShifts )
		{
			this.hideEmptyShifts = hideEmptyShifts;
		}
		
		public Boolean getHideEmptyGroups()
		{
			return this.hideEmptyGroups;
		}
		
		public Boolean getHideEmptyShifts()
		{
			return this.hideEmptyShifts;
		}
	}
}
