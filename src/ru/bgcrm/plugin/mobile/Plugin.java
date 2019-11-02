package ru.bgcrm.plugin.mobile;

import org.w3c.dom.Document;

import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.listener.DefaultProcessChangeListener.DefaultProcessorChangeContextEvent;
import ru.bgcrm.util.Setup;

public class Plugin extends ru.bgcrm.plugin.Plugin {
	public static final String ID = "mobile";
	
	public static String getServerId() { 
		return Setup.getSetup().get( Plugin.ID + ":serverId", "" );
	}

	public Plugin(Document doc) {
		super(doc, ID);
		
		EventProcessor.subscribe((e, conSet) -> 
			e.getContext().put(ID, new DefaultProcessorFunctions()),
		DefaultProcessorChangeContextEvent.class);
	}
}
