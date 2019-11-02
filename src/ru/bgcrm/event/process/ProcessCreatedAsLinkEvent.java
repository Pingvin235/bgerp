package ru.bgcrm.event.process;

import ru.bgcrm.event.UserEvent;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.struts.form.DynActionForm;

/**
 * Процесс создан привязанным другому процессу.
 */
public class ProcessCreatedAsLinkEvent extends UserEvent {
	// процесс к которому привязан процесс
	private final Process linkedProcess;
	// сам созданный процесс
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
