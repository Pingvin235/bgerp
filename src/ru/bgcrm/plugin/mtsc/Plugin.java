package ru.bgcrm.plugin.mtsc;

import org.w3c.dom.Document;

import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.listener.DefaultProcessChangeListener.DefaultProcessorChangeContextEvent;
import ru.bgcrm.event.listener.EventListener;
import ru.bgcrm.model.BGException;
import ru.bgcrm.util.sql.ConnectionSet;

public class Plugin
	extends ru.bgcrm.plugin.Plugin
{
	public static final String ID = "mtsc";
	
	public Plugin( Document doc )
	{
		super( doc, ID );
		EventProcessor.subscribe( new EventListener<DefaultProcessorChangeContextEvent>()
		{
			@Override
			public void notify( DefaultProcessorChangeContextEvent e, ConnectionSet connectionSet )
			    throws BGException
			{
				e.getContext().put( ID, new DefaultProcessorFunctions());
			}
		}, DefaultProcessorChangeContextEvent.class );
	}
}
