package ru.bgcrm.plugin.fulltext;

import org.bgerp.app.cfg.Setup;
import org.bgerp.app.event.EventProcessor;
import org.bgerp.app.event.iface.Event;

import ru.bgcrm.event.MessageRemovedEvent;
import ru.bgcrm.event.ParamChangedEvent;
import ru.bgcrm.event.customer.CustomerChangedEvent;
import ru.bgcrm.event.customer.CustomerRemovedEvent;
import ru.bgcrm.event.process.ProcessChangedEvent;
import ru.bgcrm.event.process.ProcessMessageAddedEvent;
import ru.bgcrm.event.process.ProcessRemovedEvent;
import ru.bgcrm.model.customer.Customer;
import ru.bgcrm.model.message.Message;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.plugin.fulltext.dao.SearchDAO;
import ru.bgcrm.plugin.fulltext.model.Config;
import ru.bgcrm.util.sql.ConnectionSet;

public class EventListener implements org.bgerp.app.event.iface.EventListener<Event> {

    public EventListener() {
        EventProcessor.subscribe(this, ParamChangedEvent.class);
        EventProcessor.subscribe(this, CustomerChangedEvent.class);
        EventProcessor.subscribe(this, CustomerRemovedEvent.class);
        EventProcessor.subscribe(this, ProcessChangedEvent.class);
        EventProcessor.subscribe(this, ProcessRemovedEvent.class);
        EventProcessor.subscribe(this, ProcessMessageAddedEvent.class);
        EventProcessor.subscribe(this, MessageRemovedEvent.class);
    }

    @Override
    public void notify(Event e, ConnectionSet conSet) throws Exception {
        Config config = Setup.getSetup().getConfig(Config.class);
        if (config.getObjectTypeMap().isEmpty())
            return;

        SearchDAO dao = new SearchDAO(conSet.getConnection());

        if (e instanceof ParamChangedEvent) {
            ParamChangedEvent event = (ParamChangedEvent) e;
            if (config.isParamConfigured(event.getParameter()))
                dao.scheduleUpdate(event.getParameter().getObject(), event.getObjectId());
        } else if (e instanceof CustomerChangedEvent) {
            CustomerChangedEvent event = (CustomerChangedEvent) e;
            if (config.getObjectTypeMap().containsKey(Customer.OBJECT_TYPE))
                dao.scheduleUpdate(Customer.OBJECT_TYPE, event.getCustomerId());
        } else if (e instanceof CustomerRemovedEvent) {
            CustomerRemovedEvent event = (CustomerRemovedEvent) e;
            dao.delete(Customer.OBJECT_TYPE, event.getCustomerId());
        } else if (e instanceof ProcessChangedEvent) {
            ProcessChangedEvent event = (ProcessChangedEvent) e;
            if (config.getObjectTypeMap().containsKey(Process.OBJECT_TYPE))
                dao.scheduleUpdate(Process.OBJECT_TYPE, event.getProcess().getId());
        } else if (e instanceof ProcessRemovedEvent) {
            ProcessRemovedEvent event = (ProcessRemovedEvent) e;
            dao.delete(Process.OBJECT_TYPE, event.getProcess().getId());
        } else if (e instanceof ProcessMessageAddedEvent) {
            ProcessMessageAddedEvent event = (ProcessMessageAddedEvent) e;
            if (config.getObjectTypeMap().containsKey(Message.OBJECT_TYPE))
                dao.scheduleUpdate(Message.OBJECT_TYPE, event.getMessage().getId());
        } else if (e instanceof MessageRemovedEvent) {
            MessageRemovedEvent event = (MessageRemovedEvent) e;
            if (config.getObjectTypeMap().containsKey(Message.OBJECT_TYPE))
                dao.delete(Message.OBJECT_TYPE, event.getMessageId());
        }
    }

}
