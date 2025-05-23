package ru.bgcrm.event.process;

import org.bgerp.event.base.UserEvent;
import org.bgerp.model.msg.Message;

import ru.bgcrm.model.process.Process;
import ru.bgcrm.struts.form.DynActionForm;

/**
 * New message, added to a process.
 *
 * @author Shamil Vakhitov
 */
public class ProcessMessageAddedEvent extends UserEvent {
    private final Message message;
    private final Process process;

    public ProcessMessageAddedEvent(DynActionForm form, Message message, Process process) {
        super(form);
        this.message = message;
        this.process = process;
    }

    public Message getMessage() {
        return message;
    }

    public Process getProcess() {
        return process;
    }
}
