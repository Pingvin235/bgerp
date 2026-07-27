package ru.bgcrm.event.process;

import org.bgerp.event.base.UserEvent;

import ru.bgcrm.model.process.Process;
import ru.bgcrm.struts.form.DynActionForm;

/**
 * Process is created linked to another process
 */
public class ProcessCreatedAsLinkEvent extends UserEvent {
    // the process this process is linked to
    private final Process linkedProcess;
    // the created process itself
    private final Process process;

    public ProcessCreatedAsLinkEvent(DynActionForm form, Process linkedProcess, Process process) {
        super(form);
        this.linkedProcess = linkedProcess;
        this.process = process;
    }

    public Process getLinkedProcess() {
        return linkedProcess;
    }

    public Process getProcess() {
        return process;
    }
}
