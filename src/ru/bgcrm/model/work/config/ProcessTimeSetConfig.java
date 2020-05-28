package ru.bgcrm.model.work.config;

import ru.bgcrm.cache.ParameterCache;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.param.Parameter;
import ru.bgcrm.model.work.config.CallboardConfig.Callboard;
import ru.bgcrm.util.Config;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Setup;

public class ProcessTimeSetConfig
	extends Config
{
	private static final String CONFIG_PREFIX = "callboard.timeset.";
	
	private final Callboard callboard;
	private final Parameter param;
	private final int daysShow;
	private final int changeStatusToId;
	
	public ProcessTimeSetConfig( ParameterMap configMap )
		throws BGException
	{
		super( configMap );
		
		callboard = Setup.getSetup().getConfig( CallboardConfig.class ).get( configMap.getInt( CONFIG_PREFIX + "graphId", 0 ) );
		param = ParameterCache.getParameter( configMap.getInt( CONFIG_PREFIX + "paramId", 0 ) );
		daysShow = configMap.getInt( CONFIG_PREFIX + "daysShow", 3 );
		changeStatusToId = configMap.getInt( CONFIG_PREFIX + "changeStatusToId", -1 );
	}

	public Callboard getCallboard()
	{
		return callboard;
	}

	public Parameter getParam()
	{
		return param;
	}

	public int getDaysShow()
	{
		return daysShow;
	}

	public int getChangeStatusToId()
	{
		return changeStatusToId;
	}
}
