package ru.bgcrm.event;

import org.bgerp.event.base.UserEvent;

import ru.bgcrm.struts.form.DynActionForm;

/**
 * Event generated when a dynamic class is launched via an HTTP request
 */
public class RunClassRequestEvent extends UserEvent {
    public RunClassRequestEvent(DynActionForm form) {
        super(form);
    }
}
