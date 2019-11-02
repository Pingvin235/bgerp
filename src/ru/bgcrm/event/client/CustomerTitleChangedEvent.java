package ru.bgcrm.event.client;

/**
 * Событие о изменении в названии контрагента, 
 * для обновления упоминаний контрагента в браузере клиента.
 */
public class CustomerTitleChangedEvent
    extends ClientEvent
{
	private int id;
	private String title;
	
	public CustomerTitleChangedEvent( int id, String title )
	{
		this.id = id;
		this.title = title;
	}

	public int getId()
	{
		return id;
	}

	public String getTitle()
	{
		return title;
	}
}
