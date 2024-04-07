package ru.bgcrm.event.client;

import org.bgerp.event.base.ClientEventWithId;

public class MessageTypeStateEvent extends ClientEventWithId {
    /** In sync. */
    public static final String STATE_SYNC = "SYNC";
    /** Queued for sync. */
    public static final String STATE_QUEUE = "QUEUE";

    private final String state;
    private final int unseenCount;

    public MessageTypeStateEvent(int id, String state, int unseenCount) {
        super(id);
        this.state = state;
        this.unseenCount = unseenCount;
    }

    public String getState() {
        return state;
    }

    public int getUnseenCount() {
        return unseenCount;
    }
}
