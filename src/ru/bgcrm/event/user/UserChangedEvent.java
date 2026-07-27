package ru.bgcrm.event.user;

import org.bgerp.event.base.UserEvent;

import ru.bgcrm.model.user.User;
import ru.bgcrm.struts.form.DynActionForm;

/**
 * Event is generated after user properties are changed in the editor
 */
public class UserChangedEvent extends UserEvent {
    private final User user;

    public UserChangedEvent(DynActionForm form, User user) {
        super(form);
        this.user = user;
    }

    /**
     * @return the changed user
     */
    public User getChangedUser() {
        return user;
    }
}
