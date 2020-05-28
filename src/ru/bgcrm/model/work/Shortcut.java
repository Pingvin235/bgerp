package ru.bgcrm.model.work;

import ru.bgcrm.model.IdTitle;

public class Shortcut
	extends IdTitle
{
	private String value;

	public Shortcut( int id, String title, String value )
	{
		super( id, title );
		this.value = value;
	}

	public String getValue()
	{
		return value;
	}

	public void setValue( String value )
	{
		this.value = value;
	}
}
