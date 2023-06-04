package ru.bgcrm.plugin.bgbilling.proto.model;

import java.util.Date;

import org.bgerp.model.base.Id;

import ru.bgcrm.model.PeriodSet;

public class ContractService
	extends Id
	implements PeriodSet
{
	private int contractId;
	private int serviceId;
	private String serviceTitle;
	private Date dateFrom;
	private Date dateTo;
	private String comment;

	public int getContractId()
	{
		return contractId;
	}

	public void setContractId( int contractId )
	{
		this.contractId = contractId;
	}

	public int getServiceId()
	{
		return serviceId;
	}

	public void setServiceId( int serviceId )
	{
		this.serviceId = serviceId;
	}

	public String getServiceTitle()
	{
		return serviceTitle;
	}

	public void setServiceTitle( String serviceTitle )
	{
		this.serviceTitle = serviceTitle;
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

	public String getComment()
	{
		return comment;
	}

	public void setComment( String comment )
	{
		this.comment = comment;
	}
}