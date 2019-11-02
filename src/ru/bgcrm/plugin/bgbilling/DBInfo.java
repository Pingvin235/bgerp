package ru.bgcrm.plugin.bgbilling;

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import ru.bgcrm.cache.UserCache;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.user.User;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.sql.ConnectionPool;

/**
 * Класс с данными для подсоединения к биллингу.
 */
public class DBInfo
{
	private String id;
	private String url;
	private URL serverUrl;
	private String title;
	private String version = "5.1";
	private ParameterMap setup;
	private Set<String> pluginSet;
	private ConnectionPool connectionPool;
		
	private Map<Integer, Integer> billingUserIdCrmUserIdMap = new HashMap<>();
	private ParameterMap guiConfigValues;

	public DBInfo( String id )
	{
		this.id = id;
		
		for( User user : UserCache.getUserMap().values() )
		{
			int billingUserId = user.getConfigMap().getInt( "bgbilling:userId." + id, 0 );
			if( billingUserId > 0 )
			{
				billingUserIdCrmUserIdMap.put( billingUserId, user.getId() );
			}				
		}
	}
	
	public String getId()
	{
		return id;
	}

	public String getUrl()
	{
		return url;
	}

	public URL getServerUrl()
	{
		return serverUrl;
	}

	public void setUrl( String url )
		throws BGException
	{
		this.url = url;
		try
		{
			serverUrl = new URL( url );
		}
		catch( MalformedURLException ex )
		{
			throw new BGException( ex );
		}
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle( String title )
	{
		this.title = title;
	}

	public String getVersion()
	{
		return version;
	}

	public void setVersion( String version )
	{
		this.version = version;
	}
	
	public int versionCompare( String withVersion )
	{
		return new BigDecimal( this.version ).compareTo( new BigDecimal( withVersion ) );
	}

	public ParameterMap getSetup()
	{
		return setup;
	}

	public void setSetup( ParameterMap setup )
	{
		this.setup = setup;
		this.connectionPool = new ConnectionPool( "bgbilling-pool-" + getId(), setup );
	}

	public ConnectionPool getConnectionPool()
	{
		return connectionPool;
	}

	// код текстового параметра с кодом контрагента
	public int getCustomerIdParam()
	{
		return setup.getInt( "customerIdParam", 0 );
	}
	
	public Set<String> getPluginSet()
	{
		return pluginSet;
	}

	public void setPluginSet( Set<String> pluginSet )
	{
		this.pluginSet = pluginSet;
	}
	
	public ParameterMap getGuiConfigValues()
	{
		return guiConfigValues;
	}
	
	public void setGuiConfigValues(ParameterMap config)
	{
		guiConfigValues = config;
	}

	public TransferData getTransferData()
	{
		return new TransferData( this );
	}

	public String getCopyParamMapping()
	{
		return setup.get( "copyParamMapping" );
	}

	public int getBillingUserId( User user )
	{
		return user.getConfigMap().getInt( "bgbilling:userId." + id, -1 );		
	}
	
	public int getCrmUserId( int billingUserId )
	{
		Integer value = billingUserIdCrmUserIdMap.get( billingUserId );		
		return value != null ? value : -1;		
	}
}