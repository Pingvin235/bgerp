package org.bgerp.event.client;

import org.bgerp.event.base.ClientEvent;
import org.bgerp.model.base.Message;

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
