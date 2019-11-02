package ru.bgcrm.plugin.bgbilling.proto.model.status;

import java.util.Date;

public class ContractStatusLogItem
	extends ContractStatus
{
	private String user;
	private Date time;

	public String getUser()
	{
		return user;
	}

	public void setUser( String user )
	{
		this.user = user;
	}

	public Date getTime()
	{
		return time;
	}

	public void setTime( Date time )
	{
		this.time = time;
	}
}
