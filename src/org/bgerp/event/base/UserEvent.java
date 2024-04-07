package org.bgerp.event.base;

import ru.bgcrm.model.user.User;
import ru.bgcrm.struts.form.DynActionForm;

/**
 * Event, appeared on a user's action call.
 * The class is temporary inherited from a deprecated one for backward compatibility reasons.
 * Later that parent will be removed and everything moved to the current class.
 *
 * @author Shamil Vakhitov
 */
public class UserEvent extends ru.bgcrm.event.UserEvent {
    public UserEvent(DynActionForm form) {
        super(form);
    }

    public DynActionForm getForm() {
        return form;
    }

    public User getUser() {
        return form.getUser();
    }
}
