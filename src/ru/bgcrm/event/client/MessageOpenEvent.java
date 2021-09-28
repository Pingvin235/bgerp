package ru.bgcrm.event.client;

import ru.bgcrm.model.message.Message;

/**
 * Open message for processing.
 * 
 * @author Shamil Vakhitov
 */
public class MessageOpenEvent extends ClientEvent {
    private final Message message;

    public MessageOpenEvent(Message message) {
        this.message = message;
    }

    public int getTypeId() {
        return message.getTypeId();
    }

    public String getSystemId() {
        return message.getSystemId();
    }
}
