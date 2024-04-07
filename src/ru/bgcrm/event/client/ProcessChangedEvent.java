package ru.bgcrm.event.client;

import org.bgerp.event.base.ClientEventWithId;

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
