package org.bgerp.event.base;

import org.bgerp.app.event.iface.Event;

import ru.bgcrm.model.user.User;
import ru.bgcrm.struts.form.DynActionForm;

/**
 * Event, appeared during a user's action call
 *
 * @author Shamil Vakhitov
 */
public class UserEvent implements Event {
    protected final DynActionForm form;
    private boolean processing = true;

    public UserEvent(DynActionForm form) {
        this.form = form;
    }

    public DynActionForm getForm() {
        return form;
    }

    public User getUser() {
        return form.getUser();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean processing() {
        return processing;
    }

    /**
     * Stops the event processing.
     */
    public void stopProcessing() {
        processing = false;
    }
}
