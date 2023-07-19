package ru.bgcrm.plugin.bgbilling.proto.model;

import org.bgerp.app.cfg.ConfigMap;

public class ContractParameter
{
	private int paramId = -1;
	private int paramType = -1;
	private String title;
	private String value;

	private ConfigMap configMap;

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

	public ConfigMap getConfigMap()
	{
		return configMap;
	}

	public void setConfigMap( ConfigMap configMap )
	{
		this.configMap = configMap;
	}
}
