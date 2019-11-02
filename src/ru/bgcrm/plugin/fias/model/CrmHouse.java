package ru.bgcrm.plugin.fias.model;

public class CrmHouse
{
	private Integer id;
	private Integer streetId;
	private String number;
	private String frac;
	private String comment;
	private String postalCode;

	public Integer getId()
	{
		return id;
	}

	public void setId( Integer id )
	{
		this.id = id;
	}

	public Integer getStreetId()
	{
		return streetId;
	}

	public void setStreetId( Integer streetId )
	{
		this.streetId = streetId;
	}

	public String getNumber()
	{
		return number;
	}

	public void setNumber( String number )
	{
		this.number = number;
	}

	public String getFrac()
	{
		return frac;
	}

	public void setFrac( String frac )
	{
		this.frac = frac;
	}

	public String getComment()
	{
		return comment;
	}

	public void setComment( String comment )
	{
		this.comment = comment;
	}

	public String getPostalCode()
	{
		return postalCode;
	}

	public void setPostalCode( String postalCode )
	{
		this.postalCode = postalCode;
	}
}
