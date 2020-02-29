package ru.bgcrm.event.process;

import ru.bgcrm.event.UserEvent;
import ru.bgcrm.model.message.Message;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.struts.form.DynActionForm;

/**
 * В процесс добавлено сообщение, автоматически соотнесено процессу.
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
