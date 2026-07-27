package ru.bgcrm.event.client;

import org.bgerp.event.base.ClientEvent;

/**
 * Event about a change in the customer's title, for updating mentions of the customer in the client's browser
 */
public class CustomerTitleChangedEvent extends ClientEvent {
    private int id;
    private String title;

    public CustomerTitleChangedEvent(int id, String title) {
        this.id = id;
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }
}
