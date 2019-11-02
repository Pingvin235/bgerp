package ru.bgcrm.plugin.bgbilling.proto.model;

import java.util.List;

public class BillingUser
{
	private String describe;
	private String email;
	private String groupsTitle;
	private int id;
	private String login;
	private String name;
	private String statusTitle;
	private String title;
	private String userActionText;
	private List<Integer> groups;

	public List<Integer> getGroups()
	{
		return groups;
	}

	public void setGroups( List<Integer> groups )
	{
		this.groups = groups;
	}

	public String getEmail()
	{
		return email;
	}

	public void setEmail( String email )
	{
		this.email = email;
	}

	public String getDescribe()
	{
		return describe;
	}

	public void setDescribe( String describe )
	{
		this.describe = describe;
	}

	public String getGroupsTitle()
	{
		return groupsTitle;
	}

	public void setGroupsTitle( String groupsTitle )
	{
		this.groupsTitle = groupsTitle;
	}

	public int getId()
	{
		return id;
	}

	public void setId( int id )
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

	public String getName()
	{
		return name;
	}

	public void setName( String name )
	{
		this.name = name;
	}

	public String getStatusTitle()
	{
		return statusTitle;
	}

	public void setStatusTitle( String statusTitle )
	{
		this.statusTitle = statusTitle;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle( String title )
	{
		this.title = title;
	}

	public String getUserActionText()
	{
		return userActionText;
	}

	public void setUserActionText( String userActionText )
	{
		this.userActionText = userActionText;
	}
}
