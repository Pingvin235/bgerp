package ru.bgcrm.plugin.phpbb.model;

import ru.bgcrm.util.sql.ConnectionPool;

public class Forum
{
	private String id;
	private String url;
	private ConnectionPool connectionPool;

	public String getId()
	{
		return id;
	}

	public void setId( String id )
	{
		this.id = id;
	}

	public String getUrl()
	{
		return url;
	}

	public void setUrl( String url )
	{
		this.url = url;
	}

	public ConnectionPool getConnectionPool()
	{
		return connectionPool;
	}

	public void setConnectionPool( ConnectionPool connectionPool )
	{
		this.connectionPool = connectionPool;
	}
}
