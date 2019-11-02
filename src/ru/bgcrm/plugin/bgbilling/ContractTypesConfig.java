package ru.bgcrm.plugin.bgbilling;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import ru.bgcrm.plugin.bgbilling.model.ContractType;
import ru.bgcrm.util.Config;
import ru.bgcrm.util.ParameterMap;

public class ContractTypesConfig
	extends Config
{
	private SortedMap<Integer, ContractType> typeMap = new TreeMap<Integer, ContractType>();

	public ContractTypesConfig( ParameterMap config )
    {
		this( config, "bgbilling:contractType." );
    }
	
	public ContractTypesConfig( ParameterMap config, String prefix )
    {
		super( config );
		
		for( Map.Entry<Integer, ParameterMap> me : config.subIndexed( prefix ).entrySet() )
		{
			int id = me.getKey();
			ParameterMap param = me.getValue(); 
			typeMap.put( id, new ContractType( id, param ) );
		}
    }
	
	public Map<Integer, ContractType> getTypeMap()
	{
		return typeMap;
	}
}
