package ru.bgcrm.plugin.bgbilling;

import org.w3c.dom.Document;

import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.SetupChangedEvent;
import ru.bgcrm.event.listener.EventListener;
import ru.bgcrm.event.user.UserChangedEvent;
import ru.bgcrm.plugin.bgbilling.event.listener.HelpDeskListener;
import ru.bgcrm.plugin.bgbilling.event.listener.LinkChangedListener;
import ru.bgcrm.plugin.bgbilling.event.listener.LinkChangingListener;
import ru.bgcrm.plugin.bgbilling.event.listener.ProcessDoActionListener;
import ru.bgcrm.plugin.bgbilling.event.listener.RegisterExtensionListener;
import ru.bgcrm.util.sql.ConnectionSet;

public class Plugin
	extends ru.bgcrm.plugin.Plugin
{
	public static final String ID = "bgbilling";
	
	public Plugin( Document doc )
    {
	    super( doc, ID );
	    
	    EventProcessor.subscribe( new EventListener<SetupChangedEvent>()
	    {
	    	@Override
	    	public void notify( SetupChangedEvent e, ConnectionSet connectionSet )
	    	{
	    		DBInfoManager.flush();
	    	}
	    }, SetupChangedEvent.class );
	    
	    EventProcessor.subscribe( new EventListener<UserChangedEvent>()
	    {
	    	@Override
	    	public void notify( UserChangedEvent e, ConnectionSet connectionSet )
	    	{
	    		DBInfoManager.flush();
	    	}
	    }, UserChangedEvent.class );
	    
	    new LinkChangingListener();
	    
	    new LinkChangedListener();
	    
	    new HelpDeskListener();
	    
	    new ProcessDoActionListener();
	    
	    // регистрация функций - расширений для XSLT генерации документов только если стоит плагин Document
	    try
		{
			Class.forName( "ru.bgcrm.plugin.document.Plugin" );
			new RegisterExtensionListener();
		}
		catch( ClassNotFoundException e )
		{}
    }
	
	public DBInfoManager getDbInfoManager()
    {
        return DBInfoManager.getInstance();
    }
}
