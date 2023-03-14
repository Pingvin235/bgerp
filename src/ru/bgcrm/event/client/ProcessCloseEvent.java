package ru.bgcrm.event.client;

/**
 * The opened process UI entity has to be closed.
 *
 * @author Shamil Vakhitov
 */
public class ProcessCloseEvent extends ClientEventWithId {
    public ProcessCloseEvent(int id) {
        super(id);
    }
}
