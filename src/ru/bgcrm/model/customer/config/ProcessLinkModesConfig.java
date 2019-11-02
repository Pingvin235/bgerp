package ru.bgcrm.model.customer.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.bgcrm.util.Config;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Utils;

public class ProcessLinkModesConfig
	extends Config
{
	private final Map<String, String> modeMap = new HashMap<String, String>();
	private final List<String[]> modeList = new ArrayList<String[]>();
	
	public ProcessLinkModesConfig( ParameterMap setup )
	{
		super( setup );
		for( String token : Utils.toList( setup.get( "processCustomerLinkRoles", "customer:Контрагент" ) ) )
		{
			String[] pair = token.trim().split( ":" );
			if( pair.length == 2 )
			{
				modeMap.put( pair[0], pair[1] );
				modeList.add( pair );
			}
		}
	}

	public Map<String, String> getModeMap()
	{
		return modeMap;
	}

	public List<String[]> getModeList()
	{
		return modeList;
	}	
}
