package ru.bgcrm.plugin.slack;

import org.w3c.dom.Document;

import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.listener.DefaultProcessChangeListener.DefaultProcessorChangeContextEvent;
import ru.bgcrm.util.sql.ConnectionSet;

public class Plugin extends ru.bgcrm.plugin.Plugin {
	public static final String ID = "slack";
	
	public static final String LINK_TYPE_CHANNEL = "slack-channel";

	public Plugin(Document doc) {
		super(doc, ID);
		
		EventProcessor.subscribe((DefaultProcessorChangeContextEvent e, ConnectionSet conSet) -> {
			e.getContext().put(ID, new DefaultProcessorFunctions());
		}, DefaultProcessorChangeContextEvent.class );
	}
}
