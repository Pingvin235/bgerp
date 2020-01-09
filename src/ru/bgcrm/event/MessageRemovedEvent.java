package ru.bgcrm.event;

import ru.bgcrm.struts.form.DynActionForm;

public class MessageRemovedEvent extends UserEvent {
    private final int messageId;

    public MessageRemovedEvent(DynActionForm form, int messageId) {
        super(form);
        this.messageId = messageId;
    }

    public int getMessageId() {
        return messageId;
    }
}
