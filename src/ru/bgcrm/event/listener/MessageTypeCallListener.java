package ru.bgcrm.event.listener;

import java.util.Date;

import ru.bgcrm.dao.message.MessageType;
import ru.bgcrm.dao.message.MessageTypeCall;
import ru.bgcrm.dao.message.MessageTypeCall.CallRegistration;
import ru.bgcrm.dao.message.config.MessageTypeConfig;
import ru.bgcrm.event.Event;
import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.GetPoolTasksEvent;
import ru.bgcrm.event.client.MessageOpenEvent;
import ru.bgcrm.model.BGException;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.sql.ConnectionSet;

/**
 * Обработчик событий {@link GetPoolTasksEvent},
 * передаёт на клиентскую сторону оповещения для открытия
 * сообщений типа Call, поступивших на занятый пользователем номер.
 */
public class MessageTypeCallListener extends DynamicEventListener {
    public MessageTypeCallListener() {
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