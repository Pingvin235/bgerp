package ru.bgcrm.plugin.phpbb.model;

import java.util.Date;

public class Topic
{
	public static final String OBJECT_TYPE_PREFIX = "phpbb-topic";
	
	private int id;
	private String title;
	private Date lastPostTime;
	private String lastPosterName;

	public int getId()
	{
		return id;
	}

	public void setId( int id )
	{
		this.id = id;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle( String title )
	{
		this.title = title;
	}

	public Date getLastPostTime()
	{
		return lastPostTime;
	}

	public void setLastPostTime( Date lastPostTime )
	{
		this.lastPostTime = lastPostTime;
	}

	public String getLastPosterName()
	{
		return lastPosterName;
	}

	public void setLastPosterName( String lastPosterName )
	{
		this.lastPosterName = lastPosterName;
	}
}