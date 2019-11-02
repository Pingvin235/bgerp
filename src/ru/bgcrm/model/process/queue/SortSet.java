package ru.bgcrm.model.process.queue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class SortSet
{
    private int comboCount;
    private List<SortMode> modeList = new ArrayList<SortMode>();
    private Map<Integer, Integer> defaultSortValues = new HashMap<Integer, Integer>();
    private SortedMap<Integer, Integer> sortValues = new TreeMap<Integer, Integer>();
    
	public int getComboCount()
	{
		return comboCount;
	}

	public void setComboCount( int comboCount )
	{
		this.comboCount = comboCount;
	}

	public List<SortMode> getModeList()
	{
		return modeList;
	}

	public void addMode( SortMode mode )
	{
		this.modeList.add( mode );
	}

	public void setDefaultSortValue( int comboNum, int value )
	{
		defaultSortValues.put( comboNum, value );
	}
	
	public void setSortValue( int comboNum, int value )
	{
		sortValues.put( comboNum, value );	
	}

	/*public int getDefaultSortValue( int comboNum )
	{
		int result = 0;
		if( defaultSortValues.containsKey( comboNum ) )
		{
			result = defaultSortValues.get( comboNum );
		}
		return result;
	}*/
	
	public SortedMap<Integer, Integer> getSortValues()
	{
		return sortValues;
	}

	public Map<Integer, Integer> getDefaultSortValues()
	{
		return defaultSortValues;
	}
}