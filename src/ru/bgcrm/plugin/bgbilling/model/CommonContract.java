package ru.bgcrm.plugin.bgbilling.model;

import java.util.Date;

import ru.bgcrm.model.Id;
import ru.bgcrm.model.param.ParameterAddressValue;

public class CommonContract
    extends Id
{
	public static final String OBJECT_TYPE = "bgbilling-commonContract";
	
	private int customerId;
	// район в пределах которого генерируется номер
	private int areaId;
	private int cityIds;
	// последовательный номер
	private int number;
	// адрес к которому привязан
	private ParameterAddressValue address;
	private String formatedNumber;
	
	private String password;
	private Date dateFrom;
	private Date dateTo;

	public int getCustomerId()
	{
		return customerId;
	}

	public void setCustomerId( int customerId )
	{
		this.customerId = customerId;
	}

	public int getAreaId()
	{
		return areaId;
	}

	public void setAreaId( int areaId )
	{
		this.areaId = areaId;
	}

	public int getCityIds()
	{
		return cityIds;
	}

	public void setCityIds( int cityIds )
	{
		this.cityIds = cityIds;
	}

	public int getNumber()
	{
		return number;
	}

	public ParameterAddressValue getAddress()
    {
    	return address;
    }

	public void setAddress( ParameterAddressValue addressValue )
    {
    	this.address = addressValue;
    }

	public void setNumber( int number )
	{
		this.number = number;
	}

	public String getFullNumber()
	{
		return "";
	}

	public String getFormatedNumber()
    {
    	return formatedNumber;
    }

	public void setFormatedNumber( String formatedNumber )
    {
    	this.formatedNumber = formatedNumber;
    }

	public String getPassword()
    {
    	return password;
    }

	public void setPassword( String password )
    {
    	this.password = password;
    }

	public Date getDateFrom()
    {
    	return dateFrom;
    }

	public void setDateFrom( Date dateFrom )
    {
    	this.dateFrom = dateFrom;
    }

	public Date getDateTo()
    {
    	return dateTo;
    }

	public void setDateTo( Date dateTo )
    {
    	this.dateTo = dateTo;
    }
}