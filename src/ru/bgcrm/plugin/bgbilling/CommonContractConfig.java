package ru.bgcrm.plugin.bgbilling;

import org.apache.commons.lang.StringUtils;
import ru.bgcrm.plugin.bgbilling.model.CommonContract;
import ru.bgcrm.util.Config;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.PatternFormatter;
import ru.bgcrm.util.PatternFormatter.PatternItemProcessor;
import ru.bgcrm.util.Utils;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CommonContractConfig
extends Config
{
	public static final String CONFIG_PREFIX = "bgbilling:commonContract.";

	private final int customerAddressParamId;
	private final int addressParamId;
	private final String format;
	private final String serviceContractTitleFormat;
	private final int numberLength;

	private final Map<Integer, Integer> cityCounterMap = new HashMap<>();

	private final Map<Integer, Integer> cityAreaMap = new HashMap<>();

	public CommonContractConfig( ParameterMap config )
	{
		super( config );

		customerAddressParamId = config.getInt( CONFIG_PREFIX + "customerAddressParamId", 0 );
		addressParamId = config.getInt( CONFIG_PREFIX + "addressParamId", 0 );
		format = config.get( CONFIG_PREFIX + "titleFormat", "(${area:00})(${number:000000})" );
		serviceContractTitleFormat = config.get( CONFIG_PREFIX + "serviceContractTitleFormat", "(${common})(${type:00})" );

		for( String token : config.get( CONFIG_PREFIX + "cityAreaIds", "" )
								  .split( ";" ) )
		{
			String[] pair = token.split( ":" );
			if( pair.length != 2 )
			{
				continue;
			}
			cityAreaMap.put( Utils.parseInt( pair[0] ), Utils.parseInt( pair[1] ) );
		}

		for( String token : config.get( CONFIG_PREFIX + "cityNumberCounterIds", "" )
								  .split( ";" ) )
		{
			String[] pair = token.split( ":" );
			if( pair.length != 2 )
			{
				continue;
			}
			cityCounterMap.put( Utils.parseInt( pair[0] ), Utils.parseInt( pair[1] ) );
		}

		int startPos = format.indexOf( "number" ) + "number".length() + 1;
		int endPos = format.indexOf( "}", startPos );
		numberLength = format.substring( startPos, endPos )
							 .length();
	}

	public int getCustomerAddressParamId()
	{
		return customerAddressParamId;
	}

	public int getAddressParamId()
	{
		return addressParamId;
	}

	public Integer getCityAreaId( int cityId )
	{
		return cityAreaMap.get( cityId );
	}

	public HashSet<Integer> getCityAreaMap()
	{
		return new HashSet<>( cityAreaMap.values() );
	}

	public Set<Integer> getAreas()
	{
		return cityAreaMap.keySet();
	}
	public String formatCommonContractNumber( final CommonContract commonContract )

	{
		String result = PatternFormatter.processPattern( format, new PatternItemProcessor()
		{
			@Override
			public String processPatternItem( String variable )
			{
				int number = 0;
				if( variable.startsWith( "area:" ) )
				{
					number = commonContract.getAreaId();
				}
				else if( variable.startsWith( "number:" ) )
				{
					number = commonContract.getNumber();
				}

				int pos = variable.indexOf( ':' );
				if( pos > 0 )
				{
					return new DecimalFormat( variable.substring( pos + 1 ) ).format( number );
				}

				return String.valueOf( number );
			}
		} );

		commonContract.setFormatedNumber( result );

		return result;
	}

	public String formatServiceContractNumber( final CommonContract commonContract, final int serviceCode )
	{
		return PatternFormatter.processPattern( serviceContractTitleFormat, new PatternItemProcessor()
		{
			@Override
			public String processPatternItem( String variable )
			{
				if( variable.equals( "common" ) )
				{
					return commonContract.getFormatedNumber();
				}
				if( variable.startsWith( "type:" ) )
				{
					return new DecimalFormat( StringUtils.substringAfter( variable, ":" ) ).format( serviceCode );
				}
				return "";
			}
		} );
	}

	public String formatServiceContractNumber( final CommonContract commonContract, final String serviceCode )
	{
		return PatternFormatter.processPattern( serviceContractTitleFormat, new PatternItemProcessor()
		{
			@Override
			public String processPatternItem( String variable )
			{
				if( variable.equals( "common" ) )
				{
					return commonContract.getFormatedNumber();
				}
				if( variable.startsWith( "type:" ) )
				{
					return serviceCode;
				}
				return "";
			}
		} );
	}

	public int getNumberLength()
	{
		return numberLength;
	}

	public Integer getCounterId( int cityId )
	{
		return cityCounterMap.get( cityId );
	}

	public Map<Integer, Integer> getCounterMap()
	{
		return new HashMap<Integer, Integer>( cityCounterMap );
	}

}
