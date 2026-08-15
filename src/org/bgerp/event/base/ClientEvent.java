package org.bgerp.event.base;

/**
 * Event sent to the front end where should be processed with a JS handler
 *
 * @author Shamil Vakhitov
 */
public class ClientEvent {
    /**
     * @return class name used as identifier on the front end
     */
    public String getClassName() {
        return this.getClass().getName();
    }
}
