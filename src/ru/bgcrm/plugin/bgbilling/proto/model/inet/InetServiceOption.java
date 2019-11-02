package ru.bgcrm.plugin.bgbilling.proto.model.inet;

import java.util.Date;

public class InetServiceOption
{
	private int serviceId;
	private int optionId;

	private Date dateFrom;
	private Date dateTo;

	public int getServiceId()
	{
		return serviceId;
	}

	public void setServiceId( int serviceId )
	{
		this.serviceId = serviceId;
	}

	public int getOptionId()
	{
		return optionId;
	}

	public void setOptionId( int optionId )
	{
		this.optionId = optionId;
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
