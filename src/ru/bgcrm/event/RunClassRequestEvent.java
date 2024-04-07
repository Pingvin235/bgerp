package ru.bgcrm.event;

import org.bgerp.event.base.UserEvent;

import ru.bgcrm.struts.form.DynActionForm;

/**
 * Событие, генерирующееся при запуске динамического класса посредством
 * HTTP запроса.
 */
public class RunClassRequestEvent extends UserEvent {
    public RunClassRequestEvent(DynActionForm form) {
        super(form);
    }
}
