package ru.bgcrm.plugin.bgbilling.proto.model;

import ru.bgcrm.util.ParameterMap;

public class ContractParameter
{
	private int paramId = -1;
	private int paramType = -1;
	private String title;
	private String value;

	private ParameterMap configMap;

	public ContractParameter( int paramId, int paramType, String paramTitle, String paramValue )
	{
		this.paramId = paramId;
		this.paramType = paramType;
		this.title = paramTitle;
		this.value = paramValue;
	}

	public int getParamId()
	{
		return paramId;
	}

	public void setParamId( int paramId )
	{
		this.paramId = paramId;
	}

	public int getParamType()
	{
		return paramType;
	}

	public void setParamType( int paramType )
	{
		this.paramType = paramType;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle( String title )
	{
		this.title = title;
	}

	public String getValue()
	{
		return value;
	}

	public void setValue( String value )
	{
		this.value = value;
	}

	public ParameterMap getConfigMap()
	{
		return configMap;
	}

	public void setConfigMap( ParameterMap configMap )
	{
		this.configMap = configMap;
	}
}
