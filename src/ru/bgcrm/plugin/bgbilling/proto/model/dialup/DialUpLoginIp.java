package ru.bgcrm.plugin.bgbilling.proto.model.dialup;

import java.util.Date;

import ru.bgcrm.model.PeriodSet;

public class DialUpLoginIp
	implements PeriodSet
{
	private String realm;
	private Date dateFrom;
	private Date dateTo;
	private String address;

	public String getRealm()
	{
		return realm;
	}

	public void setRealm( String realm )
	{
		this.realm = realm;
	}

	public Date getDateFrom()
	{
		return dateFrom;
	}
	
	@Override
	public void setDateFrom( Date dateFrom )
	{
		this.dateFrom = dateFrom;
	}

	public Date getDateTo()
	{
		return dateTo;
	}

	@Override
	public void setDateTo( Date dateTo )
	{
		this.dateTo = dateTo;
	}

	public String getAddress()
	{
		return address;
	}

	public void setAddress( String address )
	{
		this.address = address;
	}
}
