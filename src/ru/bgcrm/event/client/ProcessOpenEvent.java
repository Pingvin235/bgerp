package ru.bgcrm.event.client;

import org.bgerp.event.base.ClientEventWithId;

/**
 * Open UI process entity, or refresh it if was already open
 *
 * @author Shamil Vakhitov
 */
public class ProcessOpenEvent extends ClientEventWithId {
    public ProcessOpenEvent(int id) {
        super(id);
    }
}
