package ru.bgcrm.plugin.bgbilling.proto.model.dialup;

import java.util.Date;

import ru.bgcrm.model.Id;
import ru.bgcrm.model.PeriodSet;

public class DialUpLogin
	extends Id
	implements PeriodSet
{ 
	public static final int STATUS_ACTIVE = 0;
	public static final int STATUS_LOCKED = 1;
	
	private int contractId;
	private String contractTitle;
	private int login;
	private String password;
	private int status;
	private String statusTitle;
	private String alias;
	private Date dateFrom;
	private Date dateTo;
	private int session;
	private int objectId;
	private String comment;
	
	public int getContractId()
	{
		return contractId;
	}

	public void setContractId( int contractId )
	{
		this.contractId = contractId;
	}

	public String getContractTitle()
	{
		return contractTitle;
	}

	public void setContractTitle( String contractTitle )
	{
		this.contractTitle = contractTitle;
	}

	public int getLogin()
	{
		return login;
	}

	public void setLogin( int login )
	{
		this.login = login;
	}

	public int getStatus()
	{
		return status;
	}

	public void setStatus( int status )
	{
		this.status = status;
	}

	public String getStatusTitle()
	{
		return statusTitle;
	}

	public void setStatusTitle( String access )
	{
		this.statusTitle = access;
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

	public String getAlias()
	{
		return alias;
	}

	public void setAlias( String alias )
	{
		this.alias = alias;
	}

	public int getSession()
	{
		return session;
	}

	public void setSession( int session )
	{
		this.session = session;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword( String password )
	{
		this.password = password;
	}

	public int getObjectId()
	{
		return objectId;
	}

	public void setObjectId( int objectId )
	{
		this.objectId = objectId;
	}
}