package ru.bgcrm.event.client;

/**
 * Сообщение о том, что процесс изменился - 
 * необходимо перечитать его вкладку, если открыта.
 */
public class ProcessChangedEvent
    extends ClientEventWithId
{
	public ProcessChangedEvent( int id )
	{
		super( id );
	}	
}
