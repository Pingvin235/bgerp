package ru.bgcrm.event.client;

/**
 * Open message for processing.
 * 
 * @author Shamil Vakhitov
 */
public class MessageOpenEvent extends ClientEventWithId {
    public MessageOpenEvent(int id) {
        super(id);
    }
}
