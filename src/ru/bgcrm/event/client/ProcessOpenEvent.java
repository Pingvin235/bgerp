package ru.bgcrm.event.client;

import org.bgerp.event.base.ClientEventWithId;

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
