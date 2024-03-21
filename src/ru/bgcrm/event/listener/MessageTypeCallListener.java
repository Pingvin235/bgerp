package ru.bgcrm.event.listener;

import java.util.Date;

import org.bgerp.app.cfg.Setup;
import org.bgerp.app.exception.BGException;
import org.bgerp.util.Log;

import ru.bgcrm.dao.message.MessageType;
import ru.bgcrm.dao.message.MessageTypeCall;
import ru.bgcrm.dao.message.MessageTypeCall.CallRegistration;
import ru.bgcrm.event.Event;
import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.GetPoolTasksEvent;
import ru.bgcrm.event.client.MessageOpenEvent;
import ru.bgcrm.model.message.config.MessageTypeConfig;
import ru.bgcrm.util.sql.ConnectionSet;

/**
 * Handler of {@link GetPoolTasksEvent}, sends to the client side
 * events with type {@link MessageOpenEvent} to open message processing card.
 *
 * @author Shamil Vakhitov
 */
public class MessageTypeCallListener implements EventListener<Event> {
    private static final Log log = Log.getLog();

    private static MessageTypeCallListener instance;

    public MessageTypeCallListener() {
        if (instance != null) {
            log.warn("Attempt of creation a second singleton instance");
            return;
        }

        instance = this;

        EventProcessor.subscribe(this, GetPoolTasksEvent.class);
    }

    @Override
    public void notify(Event e, ConnectionSet connectionSet) throws BGException {
        if (!(e instanceof GetPoolTasksEvent)) {
            return;
        }

        GetPoolTasksEvent event = (GetPoolTasksEvent) e;

        MessageTypeConfig config = Setup.getSetup().getConfig(MessageTypeConfig.class);
        for (MessageType type : config.getTypeMap().values()) {
            if (!(type instanceof MessageTypeCall)) {
                continue;
            }

            CallRegistration reg = ((MessageTypeCall) type).getRegistrationByUser(event.getUser().getId());
            if (reg != null) {
                reg.setLastPooling(new Date());
                var message = reg.getMessageForOpen();
                if (message != null) {
                    event.getForm().getResponse().addEvent(new MessageOpenEvent(message));
                    reg.setMessageForOpen(null);
                }
            }
        }
    }
}