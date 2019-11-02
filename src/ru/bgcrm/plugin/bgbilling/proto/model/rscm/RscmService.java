package ru.bgcrm.plugin.bgbilling.proto.model.rscm;

import java.util.Date;

public class RscmService
{
	private int id;
	private int serviceId;
	private String serviceTitle;
	private int objectId;
	private String objectTitle;
	private Date date;
	private String comment;
	private int amount;
	private String unit;
	private int contractId;

	public int getServiceId()
	{
		return serviceId;
	}

	public void setServiceId( int sid )
	{
		this.serviceId = sid;
	}

	public String getServiceTitle()
	{
		return serviceTitle;
	}

	public void setServiceTitle( String serviceTitle )
	{
		this.serviceTitle = serviceTitle;
	}

	public int getObjectId()
	{
		return objectId;
	}

	public void setObjectId( int objectId )
	{
		this.objectId = objectId;
	}

	public String getObjectTitle()
	{
		return objectTitle;
	}

	public void setObjectTitle( String objectTitle )
	{
		this.objectTitle = objectTitle;
	}

	public int getId()
	{
		return id;
	}

	public void setId( int id )
	{
		this.id = id;
	}

	public Date getDate()
	{
		return date;
	}

	public void setDate( Date date )
	{
		this.date = date;
	}

	public String getComment()
	{
		return comment;
	}

	public void setComment( String comment )
	{
		this.comment = comment;
	}

	public int getAmount()
	{
		return amount;
	}

	public void setAmount( int count )
	{
		this.amount = count;
	}
	
	public String getUnit()
	{
		return unit;
	}

	public void setUnit( String unit )
	{
		this.unit = unit;
	}

	public int getContractId()
	{
		return contractId;
	}

	public void setContractId( int contractId )
	{
		this.contractId = contractId;
	}
}
