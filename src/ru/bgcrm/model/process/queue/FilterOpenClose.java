package ru.bgcrm.model.process.queue;

import ru.bgcrm.util.ParameterMap;

public class FilterOpenClose
	extends Filter
{
	private String defaultValue;

	public FilterOpenClose( int id, ParameterMap filter )
	{
		super( id, filter );
		defaultValue = filter.get( "defaultValue" ) ;
	}

	public String getDefaultValue()
	{
		return defaultValue;
	}
}
