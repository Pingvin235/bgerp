package ru.bgcrm.event.link;

import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.struts.form.DynActionForm;

public class LinkRemovedEvent extends LinkRemovingEvent {
    public LinkRemovedEvent(DynActionForm form, CommonObjectLink link) {
        super(form, link);
    }
}