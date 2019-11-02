package ru.bgcrm.plugin.bgbilling.proto.model.limit;

import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LimitLogItem
{
	private Date time;
	private String user;
	private BigDecimal limit;
	private String days;
	private String comment;

	@JsonProperty("date")
	public Date getTime()
	{
		return time;
	}

	public void setTime( Date time )
	{
		this.time = time;
	}

	@JsonProperty("userName")
	public String getUser()
	{
		return user;
	}

	public void setUser( String user )
	{
		this.user = user;
	}

	@JsonProperty("limitValue")
	public BigDecimal getLimit()
	{
		return limit;
	}

	public void setLimit( BigDecimal limit )
	{
		this.limit = limit;
	}

	public String getDays()
	{
		return days;
	}

	public void setDays( String days )
	{
		this.days = days;
	}

	public String getComment()
	{
		return comment;
	}

	public void setComment( String comment )
	{
		this.comment = comment;
	}
}
