package org.bgerp.app.event.client;

import org.bgerp.model.Message;

import ru.bgcrm.event.client.ClientEvent;

/**
 * License state event.
 *
 * @author Shamil Vakhitov
 */
public class LicenseEvent extends ClientEvent {
    private final Message message;
    private final boolean linkShown;

    public LicenseEvent(Message message, boolean linkShown) {
        this.message = message;
        this.linkShown = linkShown;
    }

    public Message getMessage() {
        return message;
    }

    public boolean isLinkShown() {
        return linkShown;
    }
}
