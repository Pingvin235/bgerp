package ru.bgcrm.event.client;

import org.bgerp.event.base.ClientEvent;

/**
 * Open tool's URL on the frontend.
 *
 * @author Shamil Vakhitov
 */
public class UrlOpenEvent extends ClientEvent {
    private final String url;

    public UrlOpenEvent(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
