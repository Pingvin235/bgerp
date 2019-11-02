package ru.bgcrm.plugin.fias.model;

public class FiasStreet
{
	private String id;
	private String title;
	private String shortName;
	private String postalCode;
	private Integer crmStreetId;
	private String crmStreetTitle;
	private Integer crmCityId;
	private String crmCitytitle;

	public String getId()
	{
		return id;
	}

	public void setId( String id )
	{
		this.id = id;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle( String title )
	{
		this.title = title;
	}

	public String getShortName()
	{
		return shortName;
	}

	public void setShortName( String shortName )
	{
		this.shortName = shortName;
	}

	public String getPostalCode()
	{
		return postalCode;
	}

	public void setPostalCode( String postalCode )
	{
		this.postalCode = postalCode;
	}

	public String getCrmCitytitle()
	{
		return crmCitytitle;
	}

	public void setCrmCitytitle( String crmCitytitle )
	{
		this.crmCitytitle = crmCitytitle;
	}

	public Integer getCrmCityId()
	{
		return crmCityId;
	}

	public void setCrmCityId( Integer crmCityId )
	{
		this.crmCityId = crmCityId;
	}

	public Integer getCrmStreetId()
	{
		return crmStreetId;
	}

	public void setCrmStreetId( Integer crmStreetId )
	{
		this.crmStreetId = crmStreetId;
	}

	public String getCrmStreetTitle()
	{
		return crmStreetTitle;
	}

	public void setCrmStreetTitle( String crmStreetTitle )
	{
		this.crmStreetTitle = crmStreetTitle;
	}
}
