package ru.bgcrm.plugin.bgbilling.proto.model.dialup;

import java.util.Date;

import org.bgerp.model.base.Id;

public class DialUpPeriod
	extends Id
{
	private int contractId;
	// периоды в виде строк, т.к. возможна разная точность (дни и секунды)
	private String periodFrom;
	private String periodTo;
	private Date startDate;
	private Date endDate;

	public int getContractId()
	{
		return contractId;
	}

	public void setContractId( int contractId )
	{
		this.contractId = contractId;
	}

	public String getPeriodFrom()
	{
		return periodFrom;
	}

	public void setPeriodFrom( String periodFrom )
	{
		this.periodFrom = periodFrom;
	}

	public String getPeriodTo()
	{
		return periodTo;
	}

	public void setPeriodTo( String periodTo )
	{
		this.periodTo = periodTo;
	}

	public Date getStartDate()
	{
		return startDate;
	}

	public void setStartDate( Date startDate )
	{
		this.startDate = startDate;
	}

	public Date getEndDate()
	{
		return endDate;
	}

	public void setEndDate( Date endDate )
	{
		this.endDate = endDate;
	}
}
