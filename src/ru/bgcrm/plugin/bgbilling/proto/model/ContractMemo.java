package ru.bgcrm.plugin.bgbilling.proto.model;

public class ContractMemo
	extends UserTime
{
	private int id;
	private String title;
	private String text;
	private boolean visibleForUser;

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

	public String getText()
	{
		return text;
	}

	public void setText( String text )
	{
		this.text = text;
	}
	
	public boolean isVisibleForUser()
	{
		return visibleForUser;
	}

	public void setVisibleForUser( boolean visibleForUser )
	{
		this.visibleForUser = visibleForUser;
	}	
}
