package ru.bgcrm.plugin.fias.model;

public class FiasHouse
{
	private Integer id;
	private Integer crmHouseId;
	private String crmPostalCode;
	private String fiasPostalCode;
	private String streetTitle;
	private String houseNum;
	private String houseFrac;

	public Integer getCrmHouseId()
	{
		return crmHouseId;
	}

	public void setCrmHouseId( Integer crmHouseId )
	{
		this.crmHouseId = crmHouseId;
	}

	public String getStreetTitle()
	{
		return streetTitle;
	}

	public void setStreetTitle( String streetTitle )
	{
		this.streetTitle = streetTitle;
	}

	public String getHouseNum()
	{
		return houseNum;
	}

	public void setHouseNum( String houseNum )
	{
		this.houseNum = houseNum;
	}

	public String getHouseFrac()
	{
		return houseFrac;
	}

	public void setHouseFrac( String houseFrac )
	{
		this.houseFrac = houseFrac;
	}

	public Integer getId()
	{
		return id;
	}

	public void setId( Integer id )
	{
		this.id = id;
	}

	public String getCrmPostalCode()
	{
		return crmPostalCode;
	}

	public void setCrmPostalCode( String crmPostalCode )
	{
		this.crmPostalCode = crmPostalCode;
	}

	public String getFiasPostalCode()
	{
		return fiasPostalCode;
	}

	public void setFiasPostalCode( String fiasPostalCode )
	{
		this.fiasPostalCode = fiasPostalCode;
	}
}
