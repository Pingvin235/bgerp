package ru.bgcrm.plugin.bgbilling.proto.model.bill;

import java.util.Date;

public class Bill
	extends Document
{
	private int status;
	private String statusTitle;
	private Date payDate;
	private String payUser;

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

	public void setStatusTitle( String statusTitle )
	{
		this.statusTitle = statusTitle;
	}

	public Date getPayDate()
	{
		return payDate;
	}

	public void setPayDate( Date payDate )
	{
		this.payDate = payDate;
	}

	public String getPayUser()
	{
		return payUser;
	}

	public void setPayUser( String payUser )
	{
		this.payUser = payUser;
	}
}