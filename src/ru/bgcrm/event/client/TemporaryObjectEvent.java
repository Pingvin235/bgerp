package ru.bgcrm.event.client;

import java.util.Set;

import org.bgerp.event.base.ClientEvent;

public class TemporaryObjectEvent extends ClientEvent {
    private final Set<Integer> processIds;

    public TemporaryObjectEvent(Set<Integer> processIds) {
        this.processIds = processIds;
    }

    public Set<Integer> getProcessIds() {
        return processIds;
    }
}
