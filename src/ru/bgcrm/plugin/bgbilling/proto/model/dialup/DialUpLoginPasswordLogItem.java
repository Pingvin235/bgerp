package ru.bgcrm.plugin.bgbilling.proto.model.dialup;

import java.util.Date;

public class DialUpLoginPasswordLogItem
{
	private Date time;
	private String user;

	public Date getTime()
	{
		return time;
	}

	public void setTime( Date time )
	{
		this.time = time;
	}

	public String getUser()
	{
		return user;
	}

	public void setUser( String user )
	{
		this.user = user;
	}
}
