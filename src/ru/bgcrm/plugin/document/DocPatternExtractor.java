package ru.bgcrm.plugin.document;

/*
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import ru.bgcrm.cache.ParameterCache;
import ru.bgcrm.dao.ParamValueDAO;
import ru.bgcrm.model.param.Parameter;
import ru.bgcrm.model.param.ParameterValuePair;
import ru.bgcrm.plugin.document.model.Extractor;

 * Извлекает параметры контрагента.
 
public class DocPatternExtractor
	extends Extractor
{
	private static final Logger log = Logger.getLogger( DocPatternExtractor.class );
	
	private Map<Integer, String> customerParams;
	
	private int customerId;
	
	public void setCustomerId( int customerId )
    {
    	this.customerId = customerId;
    }
	
	public Map<Integer, String> getCustomerParams()
	{
		if( customerParams == null )
		{
			customerParams = new HashMap<Integer, String>();
			try 
			{
				List<Parameter> paramList = ParameterCache.getObjectTypeParameterList( "customer", 0 );
				
				for( ParameterValuePair param : new ParamValueDAO( con ).loadParameters( paramList, customerId ) )
				{
					customerParams.put( param.getParameter().getId(), param.getValueTitle() );
				}
			} 
			catch( Exception e ) 
			{
				log.error( e.getMessage(), e );
			}			
		}
		return customerParams;
	}
}
*/