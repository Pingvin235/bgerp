package ru.bgcrm.plugin.bgbilling.proto.model.dialup;

public class DialUpLoginAttr
{
	private String realm;
	private String name;
	private String value;

	public String getRealm()
	{
		return realm;
	}

	public void setRealm( String realm )
	{
		this.realm = realm;
	}

	public String getName()
	{
		return name;
	}

	public void setName( String name )
	{
		this.name = name;
	}

	public String getValue()
	{
		return value;
	}

	public void setValue( String value )
	{
		this.value = value;
	}
}