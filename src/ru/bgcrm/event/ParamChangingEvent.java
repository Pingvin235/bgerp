package ru.bgcrm.event;

import org.bgerp.event.base.UserEvent;
import org.bgerp.model.param.Parameter;

import ru.bgcrm.struts.form.DynActionForm;

/**
 * Event is generated before a parameter change, the old parameter value is still in the database at that point
 */
public class ParamChangingEvent extends UserEvent {
    private Parameter parameter;
    private int objectId;
    private Object value;

    public ParamChangingEvent(DynActionForm form, Parameter parameter, int objectId, Object value) {
        super(form);
        this.parameter = parameter;
        this.objectId = objectId;
        this.value = value;
    }

    public Parameter getParameter() {
        return parameter;
    }

    public int getObjectId() {
        return objectId;
    }

    public Object getValue() {
        return value;
    }
}