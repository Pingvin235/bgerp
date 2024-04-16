package org.bgerp.plugin.pln.agree.event;

import org.bgerp.event.base.UserEvent;

import ru.bgcrm.model.process.Process;
import ru.bgcrm.struts.form.DynActionForm;

public class AgreementEvent extends UserEvent {
    public static enum Mode {
        START, PROGRESS, FINISH
    }

    private final Process process;
    private final Mode mode;

    public AgreementEvent(DynActionForm form, Process process, Mode mode) {
        super(form);
        this.process = process;
        this.mode = mode;
    }

    public Process getProcess() {
        return process;
    }

    public Mode getMode() {
        return mode;
    }
}
