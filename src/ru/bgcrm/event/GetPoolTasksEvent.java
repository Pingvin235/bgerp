package ru.bgcrm.event;

import org.bgerp.event.base.UserEvent;

import ru.bgcrm.struts.form.DynActionForm;

/**
 * Polling event, generated for every active user, very often.
 * Complex logic in the handler is not allowed.
 */
public class GetPoolTasksEvent extends UserEvent {
    public GetPoolTasksEvent(DynActionForm form) {
        super(form);
    }
}
