package ru.bgcrm.event.client;

/**
 * Сообщение о необходимости открыть вкладку процесса,
 * либо обновить, если она уже открыта.
 */
public class ProcessOpenEvent
	extends ClientEventWithId
{
	public ProcessOpenEvent( int id )
	{
		super( id );
	}
}
