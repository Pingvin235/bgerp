package ru.bgcrm.event.listener;

import ru.bgcrm.event.Event;
import ru.bgcrm.model.BGException;
import ru.bgcrm.util.sql.ConnectionSet;

public abstract class DynamicEventListener
	implements EventListener<Event>
{
	public abstract void notify( Event e, ConnectionSet connectionSet )
		throws BGException;
}
