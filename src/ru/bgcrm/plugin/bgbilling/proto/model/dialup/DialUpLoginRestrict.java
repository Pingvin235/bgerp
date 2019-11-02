package ru.bgcrm.plugin.bgbilling.proto.model.dialup;

import java.util.Date;
import java.util.List;

public class DialUpLoginRestrict
{
	public class RestrictTime
	{
		private List<Integer> dayOfMonth;
		private List<Integer> dayOfWeek;
		private List<Integer> activeHours;
		private List<Integer> activeMinutes;

		public List<Integer> getDayOfMonth()
		{
			return dayOfMonth;
		}

		public void setDayOfMonth( List<Integer> dayOfMonth )
		{
			this.dayOfMonth = dayOfMonth;
		}

		public List<Integer> getDayOfWeek()
		{
			return dayOfWeek;
		}

		public void setDayOfWeek( List<Integer> dayOfWeek )
		{
			this.dayOfWeek = dayOfWeek;
		}

		public List<Integer> getActiveHours()
		{
			return activeHours;
		}

		public void setActiveHours( List<Integer> activeHours )
		{
			this.activeHours = activeHours;
		}

		public List<Integer> getActiveMinutes()
		{
			return activeMinutes;
		}

		public void setActiveMinutes( List<Integer> activeMinutes )
		{
			this.activeMinutes = activeMinutes;
		}
	}

	public enum Access
	{
		ALLOW, DENY
	}

	public enum Restrict
	{
		ENTER, WORK
	}

	private int id;
	private Date dateFrom;
	private Date dateTo;
	private String comment;
	private int serviceId;
	private int type;
	private Access access;
	private Restrict restrict;
	private RestrictTime restrictTime;

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

	public int getServiceId()
	{
		return serviceId;
	}

	public void setServiceId( int serviceId )
	{
		this.serviceId = serviceId;
	}

	public int getType()
	{
		return type;
	}

	public void setType( int type )
	{
		this.type = type;
	}

	public Access getAccess()
	{
		return access;
	}

	public void setAccess( Access access )
	{
		this.access = access;
	}

	public RestrictTime getRestrictTime()
	{
		return restrictTime;
	}

	public void setRestrictTime( RestrictTime restrictTime )
	{
		this.restrictTime = restrictTime;
	}

	public Restrict getRestrict()
	{
		return restrict;
	}

	public void setRestrict( Restrict restrict )
	{
		this.restrict = restrict;
	}

}
