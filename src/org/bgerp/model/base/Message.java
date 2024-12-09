package org.bgerp.model.base;

/**
 * Message with title.
 *
 * @author Shamil Vakhitov
 */
public class Message {
    private final String title;
    private final String message;

    public Message(String title, String message) {
        this.title = title;
        this.message = message;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }
}
