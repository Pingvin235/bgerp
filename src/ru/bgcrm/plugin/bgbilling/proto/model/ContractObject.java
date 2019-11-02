package ru.bgcrm.plugin.bgbilling.proto.model;

import java.util.Date;

import ru.bgcrm.model.IdTitle;

public class ContractObject
	extends IdTitle
{
	private String period;
	private Date dateFrom;
	private Date dateTo;
	private int typeId;
	private String type;

	public String getPeriod()
	{
		return period;
	}

	public void setPeriod( String period )
	{
		this.period = period;
	}

	public String getType()
	{
		return type;
	}

	public void setType( String type )
	{
		this.type = type;
	}

	public int getTypeId()
	{
		return typeId;
	}

	public void setTypeId( int typeId )
	{
		this.typeId = typeId;
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
