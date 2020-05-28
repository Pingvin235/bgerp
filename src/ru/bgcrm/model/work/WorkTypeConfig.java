package ru.bgcrm.model.work;

import java.util.List;

import ru.bgcrm.util.Preferences;
import ru.bgcrm.util.Utils;

public class WorkTypeConfig	
{
	public static final int MODE_TIME_ON_START = 0;
	public static final int MODE_TIME_ON_STEP = 1;
	
	private String color;
	private List<String> shortcutList;
	private int step; 
	private int distributeMode;
	
	//private Map<Integer, WorkTypeRule> rulesMap = new HashMap<Integer, WorkTypeRule>();
		
	public WorkTypeConfig() 
	{}
	
	public WorkTypeConfig( String config ) 
	{
		Preferences configMap = new Preferences( config );
		
		color = configMap.get( "color", "#000000" );
		shortcutList = Utils.toList( configMap.get( "shortcuts", "" ) );
		step = configMap.getInt( "step", 0 );
		distributeMode = configMap.getInt( "distributeMode", MODE_TIME_ON_START );
		
	/*	Map<String, String> map = configMap.getHashValuesWithPrefix( "rule" );
		if( map.size() > 0 )
		{
			ParameterMap parameterMap = new DefaultParameterMap( map );
			Map<Integer, ParameterMap> sortedMap = parameterMap.subIndexed( "." );
			
			for( Entry<Integer, ParameterMap> entry : sortedMap.entrySet() )
			{
				WorkTypeRule processServiceRule = new WorkTypeRule();
				processServiceRule.setProcessIds( Utils.toIntegerSet( entry.getValue().get( "processIds" ) ) );
				processServiceRule.setServiceIds( Utils.toIntegerSet( entry.getValue().get( "serviceIds" ) ) );
				processServiceRule.setTime( Integer.valueOf( entry.getValue().get( "time" ) ) ); 
				
				rulesMap.put( entry.getKey(), processServiceRule);
			}
		}*/
	}
	
	/*public Map<Integer, WorkTypeRule> getRulesMap()
	{
		return rulesMap;
	}

	public void setRulesMap( Map<Integer, WorkTypeRule> rulesMap )
	{
		this.rulesMap = rulesMap;
	}*/

	public String getColor()
	{
		return color;
	}

	public void setColor( String color )
	{
		this.color = color;		
	}
	
	public List<String> getShortcutList()
	{
		return shortcutList;
	}

	public void setShortcutList( List<String> shortcutList )
	{
		this.shortcutList = shortcutList;
	}
	
	public int getStep()
	{
		return step;
	}

	public void setStep( int step )
	{
		this.step = step;
	}
	
	public int getDistributeMode()
	{
		return distributeMode;
	}

	public void setDistributeMode( int distributeMode )
	{
		this.distributeMode = distributeMode;
	}

	public String serializeToData()
	{
		StringBuilder result = new StringBuilder();
		
		/*for( Entry<Integer, WorkTypeRule> item : rulesMap.entrySet() ) {
			
			Utils.addSetupPair( result, "", "rule." + item.getKey() + ".processIds", Utils.toString( item.getValue().getProcessIds() ) );
			Utils.addSetupPair( result, "", "rule." + item.getKey() + ".serviceIds", Utils.toString( item.getValue().getServiceIds() ) );
			Utils.addSetupPair( result, "", "rule." + item.getKey() + ".time", String.valueOf( item.getValue().getTime() ) );
		}*/
		
		Utils.addSetupPair( result, "", "color", String.valueOf( color ) );
		Utils.addSetupPair( result, "shortcuts", "", Utils.toString( shortcutList ) );
		Utils.addSetupPair( result, "step", "", String.valueOf( step ) );
		Utils.addSetupPair( result, "distributeMode", "", String.valueOf( distributeMode ) );

		return result.toString();
	}
}