package ru.bgcrm.plugin.bgbilling.proto.model.npay;

import java.util.Date;

public class NPayService
{
	private int id;
	private int serviceId;
	private String serviceTitle;
	private int objectId;
	private String objectTitle;
	private Date dateFrom;
	private Date dateTo;
	private String comment;
	private int count;
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

	public String getComment()
	{
		return comment;
	}

	public void setComment( String comment )
	{
		this.comment = comment;
	}

	public int getCount()
	{
		return count;
	}

	public void setCount( int count )
	{
		this.count = count;
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
