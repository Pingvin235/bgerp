package ru.bgcrm.event.client;

/**
 * Process was changed, the UI entity has to be opened or updated if already is open.
 *
 * @author Shamil Vakhitov
 */
public class ProcessChangedEvent extends ClientEventWithId {
    public ProcessChangedEvent(int id) {
        super(id);
    }
}
