package ru.bgcrm.event.client;

import java.util.HashMap;
import java.util.Map;

import ru.bgcrm.model.process.queue.config.SavedFiltersConfig.SavedFilterSet;

public class FilterCounterEvent
	extends ClientEvent
{
	private Map<Integer, SavedFilterSet> filters;
	private Map<Integer, HashMap<Integer, Integer>> count;

	public void setFilters( Map<Integer, SavedFilterSet> f )
	{
		filters = f;
	}
	
	public Map<Integer, SavedFilterSet> getFilters()
	{
		return filters;
	}

	public void setCount( HashMap<Integer, HashMap<Integer, Integer>> c )
	{
		count = c;
	}
	
	public Map<Integer, HashMap<Integer, Integer>> getCount()
	{
		return count;
	}
}
