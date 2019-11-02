package ru.bgcrm.plugin.bgbilling.proto.model.limit;

import java.math.BigDecimal;
import java.util.Date;

import ru.bgcrm.model.Id;

public class LimitChangeTask
	extends Id
{
	private Date date;
	private String user;
	private BigDecimal limitChange;

	public Date getDate()
	{
		return date;
	}

	public void setDate( Date date )
	{
		this.date = date;
	}

	public String getUser()
	{
		return user;
	}

	public void setUser( String user )
	{
		this.user = user;
	}

	public BigDecimal getLimitChange()
	{
		return limitChange;
	}

	public void setLimitChange( BigDecimal limitChange )
	{
		this.limitChange = limitChange;
	}
}
