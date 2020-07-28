package ru.bgcrm.event;

import ru.bgcrm.struts.form.DynActionForm;

public class SetupChangedEvent extends UserEvent {
    public SetupChangedEvent(DynActionForm form) {
        super(form);
    }
}
