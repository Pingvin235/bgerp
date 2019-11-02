package ru.bgcrm.plugin.bgbilling.proto.model.phone;

import java.util.Date;

public class PhonePoint
{
	private int id;
	private String alias;
	private String comment;
	private String description;
	private String period;
	private int sourceId;
	private int type;
	private Date dateFrom;
	private Date dateTo;
	private String clientNumbers;
	private int objectId;

	public String getAlias()
	{
		return alias;
	}

	public void setAlias( String alias )
	{
		this.alias = alias;
	}

	public String getComment()
	{
		return comment;
	}

	public void setComment( String comment )
	{
		this.comment = comment;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription( String description )
	{
		this.description = description;
	}

	public int getId()
	{
		return id;
	}

	public void setId( int id )
	{
		this.id = id;
	}

	public int getSourceId()
	{
		return sourceId;
	}

	public void setSourceId( int sourceId )
	{
		this.sourceId = sourceId;
	}

	public String getPeriod()
	{
		return period;
	}

	public void setPeriod( String period )
	{
		this.period = period;
	}

	public int getType()
	{
		return type;
	}

	public void setType( int type )
	{
		this.type = type;
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

	public String getClientNumbers()
	{
		return clientNumbers;
	}

	public void setClientNumbers( String clinetNumbers )
	{
		this.clientNumbers = clinetNumbers;
	}

	public void setObjectId( int objectId )
	{
		this.objectId = objectId;
	}

	public int getObjectId()
	{
		return objectId;
	}
}
