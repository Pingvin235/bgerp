package ru.bgcrm.event.client;

/**
 * Open customer in UI or refresh if already opened.
 * 
 * @author Shamil Vakhitov
 */
public class CustomerOpenEvent extends ClientEventWithId {
    public CustomerOpenEvent(int id) {
        super(id);
    }
}
