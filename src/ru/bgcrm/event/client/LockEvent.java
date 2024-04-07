package ru.bgcrm.event.client;

import org.bgerp.event.base.ClientEvent;

import ru.bgcrm.model.Lock;

public class LockEvent
	extends ClientEvent
{
	private final Lock lock;

	public LockEvent( Lock lock )
	{
		this.lock = lock;
	}

	public Lock getLock()
	{
		return lock;
	}
}
