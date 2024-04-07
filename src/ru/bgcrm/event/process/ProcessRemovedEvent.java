package ru.bgcrm.event.process;

import org.bgerp.event.base.UserEvent;

import ru.bgcrm.model.process.Process;
import ru.bgcrm.struts.form.DynActionForm;

public class ProcessRemovedEvent extends UserEvent {
    private final Process process;

    public ProcessRemovedEvent(DynActionForm form, Process process) {
        super(form);
        this.process = process;
    }

    public Process getProcess() {
        return process;
    }
}