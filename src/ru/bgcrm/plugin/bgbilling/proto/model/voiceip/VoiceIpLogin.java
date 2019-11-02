package ru.bgcrm.plugin.bgbilling.proto.model.voiceip;

import java.util.Date;

public class VoiceIpLogin
{
	private Integer id;
	private String login;
	private String alias;
	private String period;
	private String type;
	private String access;
	private String comment;
	private int objectId;
	private Date dateFrom;
	private Date dateTo;
	private String password;

	public Integer getId()
	{
		return id;
	}

	public void setId( Integer id )
	{
		this.id = id;
	}

	public String getLogin()
	{
		return login;
	}

	public void setLogin( String login )
	{
		this.login = login;
	}

	public String getAlias()
	{
		return alias;
	}

	public void setAlias( String alias )
	{
		this.alias = alias;
	}

	public String getPeriod()
	{
		return period;
	}

	public void setPeriod( String period )
	{
		this.period = period;
	}

	public String getType()
	{
		return type;
	}

	public void setType( String type )
	{
		this.type = type;
	}

	public String getAccess()
	{
		return access;
	}

	public void setAccess( String access )
	{
		this.access = access;
	}

	public String getComment()
	{
		return comment;
	}

	public void setComment( String comment )
	{
		this.comment = comment;
	}

	public int getObjectId()
	{
		return objectId;
	}

	public void setObjectId( int objectId )
	{
		this.objectId = objectId;
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

	public String getPassword()
	{
		return password;
	}

	public void setPassword( String password )
	{
		this.password = password;
	}
}
