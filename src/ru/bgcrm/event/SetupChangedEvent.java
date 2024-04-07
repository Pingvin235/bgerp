package ru.bgcrm.event;

import org.bgerp.event.base.UserEvent;

import ru.bgcrm.struts.form.DynActionForm;

public class SetupChangedEvent extends UserEvent {
    public SetupChangedEvent(DynActionForm form) {
        super(form);
    }
}
