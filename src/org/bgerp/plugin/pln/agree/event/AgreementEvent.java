package org.bgerp.plugin.pln.agree.event;

import org.bgerp.event.base.UserEvent;

import ru.bgcrm.model.process.Process;
import ru.bgcrm.struts.form.DynActionForm;

public class AgreementEvent extends UserEvent {
    private final Process process;
    private final boolean done;

    public AgreementEvent(DynActionForm form, Process process, boolean done) {
        super(form);
        this.process = process;
        this.done = done;
    }

    public Process getProcess() {
        return process;
    }

    public boolean isDone() {
        return done;
    }
}
