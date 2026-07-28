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

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ProcessOpenEvent other = (ProcessOpenEvent) obj;
        if (getId() != other.getId())
            return false;
        return true;
    }
}
