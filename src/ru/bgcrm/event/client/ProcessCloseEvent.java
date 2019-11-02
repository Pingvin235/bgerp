package ru.bgcrm.event.client;

/**
 * Сообщение о необходимости закрыть вкладку процесса.
 */
public class ProcessCloseEvent
	extends ClientEventWithId
{
	public ProcessCloseEvent( int id )
	{
		super( id );
	}
}
