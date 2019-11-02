package ru.bgcrm.plugin.bgbilling.proto.model.phone;

import ru.bgcrm.model.IdTitle;

import java.util.Date;

public class PhoneResourceItem
	extends IdTitle
{
	private String comment;
	private Date dateReserve;
	private String number;
	private String period;
	private String reserveComment;

	public String getComment()
	{
		return comment;
	}
	public void setComment( String comment )
	{
		this.comment = comment;
	}
	public Date getDateReserve()
	{
		return dateReserve;
	}
	public void setDateReserve( Date dateReserve )
	{
		this.dateReserve = dateReserve;
	}
	public String getNumber()
	{
		return number;
	}
	public void setNumber( String number )
	{
		this.number = number;
	}
	public String getPeriod()
	{
		return period;
	}
	public void setPeriod( String period )
	{
		this.period = period;
	}
	public String getReserveComment()
	{
		return reserveComment;
	}
	public void setReserveComment( String reserveComment )
	{
		this.reserveComment = reserveComment;
	}	
}
