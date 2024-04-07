package ru.bgcrm.event;

import org.bgerp.event.base.UserEvent;

import ru.bgcrm.struts.form.DynActionForm;

/**
 * Событие пуллинга, генерируется для каждого работающего пользователя,
 * очень часто. Недопустима сложная логика в обработчике.
 */
public class GetPoolTasksEvent extends UserEvent {
    public GetPoolTasksEvent(DynActionForm form) {
        super(form);
    }
}
