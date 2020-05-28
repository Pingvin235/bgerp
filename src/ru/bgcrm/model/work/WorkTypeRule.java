package ru.bgcrm.model.work;

/*
import java.util.List;
import java.util.Set;

import ru.bgcrm.cache.ParameterCache;
import ru.bgcrm.cache.ProcessTypeCache;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.IdTitle;
import ru.bgcrm.util.Setup;

public class WorkTypeRule
{
	private Set<Integer> processIds;
	private Set<Integer> serviceIds;
	private int time;
		
	public Set<Integer> getProcessIds()
	{
		return processIds;
	}
	
	public void setProcessIds( Set<Integer> processIds )
	{
		this.processIds = processIds;
	}
	
	public Set<Integer> getServiceIds()
	{
		return serviceIds;
	}
	
	public void setServiceIds( Set<Integer> serviceIds )
	{
		this.serviceIds = serviceIds;
	}
	
	public int getTime()
	{
		return time;
	}
	
	public void setTime( int time )
	{
		this.time = time;
	}
	
	public String getProcessString()
	{
		String resultString = "";
		
		for( Integer item : processIds )
		{
			if( resultString.length() > 0 )
			{
				resultString+=", ";
			}
			
			resultString+=ProcessTypeCache.getProcessType( item )+" ("+item+")";
		}
		
		return resultString;
	}
	
	public String getServiceString() 
		throws BGException 
	{
		int paramater = Setup.getSetup().getInt( "callboard.serviceListId", 0 );
		
		if( paramater == 0 )
		{
			throw new BGException( "Параметр списка услуг для видов работ не найден в конфигурации" );
		}
				
		String resultString = "";
		List<IdTitle> list = ParameterCache.getParameter(paramater).getListParamValues();
		
		for( Integer item : serviceIds )
		{
			if( resultString.length() > 0 )
			{
				resultString+=", ";
			}
			
			resultString+=list.get( item-1 )+" ("+item+")";
		}
		
		return resultString;
	}
}
*/
