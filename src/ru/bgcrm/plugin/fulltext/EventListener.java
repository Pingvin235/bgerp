package ru.bgcrm.plugin.fulltext;

import ru.bgcrm.event.CustomerDeleteEvent;
import ru.bgcrm.event.CustomerUpdateEvent;
import ru.bgcrm.event.Event;
import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.ParamChangedEvent;
import ru.bgcrm.event.listener.DynamicEventListener;
import ru.bgcrm.event.process.ProcessMessageAddedEvent;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.Customer;
import ru.bgcrm.plugin.fulltext.dao.SearchDAO;
import ru.bgcrm.plugin.fulltext.model.Config;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.sql.ConnectionSet;

public class EventListener extends DynamicEventListener {
    
    public EventListener() {
        EventProcessor.subscribe(this, ParamChangedEvent.class);        
        EventProcessor.subscribe(this, CustomerUpdateEvent.class);
        EventProcessor.subscribe(this, CustomerDeleteEvent.class);
        EventProcessor.subscribe(this, ProcessMessageAddedEvent.class);
    }
    
    @Override
    public void notify(Event e, ConnectionSet conSet) throws BGException {
        Config config = Setup.getSetup().getConfig(Config.class);
        if (config.getObjectTypeMap().isEmpty())
            return;
        
        SearchDAO dao = new SearchDAO(conSet.getConnection());
        
        if (e instanceof ParamChangedEvent) {
            ParamChangedEvent event = (ParamChangedEvent) e;
            if (config.isParamConfigured(event.getParameter().getId()))
                dao.scheduleUpdate(event.getParameter().getObject(), event.getObjectId());
        } else if (e instanceof CustomerUpdateEvent) {
            CustomerUpdateEvent event = (CustomerUpdateEvent) e;
            if (config.getObjectTypeMap().containsKey(Customer.OBJECT_TYPE))
                dao.scheduleUpdate(Customer.OBJECT_TYPE, event.getCustomerId());
        } else if (e instanceof CustomerDeleteEvent) {
            CustomerDeleteEvent event = (CustomerDeleteEvent) e;
            dao.delete(Customer.OBJECT_TYPE, event.getCustomerId());
        }
    }

}
