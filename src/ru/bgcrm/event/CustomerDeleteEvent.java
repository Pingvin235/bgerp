package ru.bgcrm.event;

import ru.bgcrm.event.customer.CustomerRemovedEvent;
import ru.bgcrm.struts.form.DynActionForm;

@Deprecated
public class CustomerDeleteEvent extends CustomerRemovedEvent {
    public CustomerDeleteEvent(DynActionForm form, int customerId) {
        super(form, customerId);
    }
}
