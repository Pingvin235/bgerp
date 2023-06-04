package ru.bgcrm.plugin.bgbilling.proto.model.tariff;

import java.util.Date;

import org.bgerp.model.base.Id;

import ru.bgcrm.model.PeriodSet;

public class ContractTariffGroup
	extends Id
	implements PeriodSet
{
	private int groupId;
	private Date dateFrom;
	private Date dateTo;
	private String title;
	private String comment;

	public String getTitle()
	{
		return title;
	}

	public void setTitle( String title )
	{
		this.title = title;
	}

	public String getComment()
	{
		return comment;
	}

	public void setComment( String comment )
	{
		this.comment = comment;
	}

	public int getGroupId()
	{
		return groupId;
	}

	public void setGroupId( int groupId )
	{
		this.groupId = groupId;
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
}