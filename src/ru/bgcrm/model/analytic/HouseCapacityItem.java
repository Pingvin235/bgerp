package ru.bgcrm.model.analytic;

import java.util.Date;

public class HouseCapacityItem
{
	private int houseId = -1;
	private Date date;
	private String serviceType;
	private int value = 0;

	public int getHouseId()
	{
		return houseId;
	}

	public void setHouseId( int houseId )
	{
		this.houseId = houseId;
	}

	public String getServiceType()
	{
		return serviceType;
	}

	public void setServiceType( String serviceType )
	{
		this.serviceType = serviceType;
	}

	public Date getDate()
	{
		return date;
	}

	public void setDate( Date date )
	{
		this.date = date;
	}

	public int getValue()
	{
		return value;
	}

	public void setValue( int value )
	{
		this.value = value;
	}
}
