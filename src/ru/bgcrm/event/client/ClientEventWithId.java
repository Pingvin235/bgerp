package ru.bgcrm.event.client;

public class ClientEventWithId
	extends ClientEvent
{
	private final int id;
	
	public ClientEventWithId( int id )
	{
		this.id = id;
	}

	public int getId()
    {
    	return id;
    }	
}
