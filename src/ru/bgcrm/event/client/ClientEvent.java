package ru.bgcrm.event.client;

/**
 * Event sent to the front end where should be processed with a JS handler.
 * 
 * @author Shamil Vakhitov
 */
public class ClientEvent {
    /**
     * Class name used as identifier on the front end.
     * @return
     */
    public String getClassName() {
        return this.getClass().getName();
    }
}
