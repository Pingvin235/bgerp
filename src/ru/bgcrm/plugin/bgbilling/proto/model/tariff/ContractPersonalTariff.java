package ru.bgcrm.plugin.bgbilling.proto.model.tariff;

import java.util.Date;

import org.bgerp.model.base.Id;

import ru.bgcrm.model.PeriodSet;

public class ContractPersonalTariff
	extends Id
	implements PeriodSet
{
	private int position;
	private Date dateFrom;
	private Date dateTo;
	private String title;
	private int treeId;

	public int getPosition()
	{
		return position;
	}

	public void setPosition( int position )
	{
		this.position = position;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle( String title )
	{
		this.title = title;
	}

	public Integer getTreeId()
	{
		return treeId;
	}

	public void setTreeId( Integer treeId )
	{
		this.treeId = treeId;
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