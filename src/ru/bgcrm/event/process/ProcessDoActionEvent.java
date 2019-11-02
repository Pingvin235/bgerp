package ru.bgcrm.event.process;

import ru.bgcrm.event.UserEvent;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.struts.form.DynActionForm;

public class ProcessDoActionEvent extends UserEvent {
	private Process process;
	private String actionName;
	private String forward;

	public ProcessDoActionEvent(DynActionForm form, Process process, String actionName) {
		super(form);
		this.process = process;
		this.actionName = actionName;
	}

	public Process getProcess() {
		return process;
	}

	public String getActionName() {
		return actionName;
	}

	public String getForward() {
		return forward;
	}

	public void setForward(String forward) {
		this.forward = forward;
	}
}
