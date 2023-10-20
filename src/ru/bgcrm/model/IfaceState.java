package ru.bgcrm.model;

import java.util.Arrays;

import org.bgerp.util.Dynamic;

import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;

/**
 * Interface element for an object: process, customer, etc.
 * For example, counts of linked processes to a process to be shown on the related tab.
 *
 * @author Shamil Vakhitov
 */
public class IfaceState {
    public static final String REQUEST_PARAM_IFACE_ID = "ifaceId";
    public static final String REQUEST_PARAM_STATE = "ifaceState";

    private String objectType;
    private int objectId;
    private String ifaceId;
    /** State string with comma separated values. */
    private String state;

    public IfaceState(String ifaceId, String state) {
        this.ifaceId = ifaceId;
        this.state = state;
    }

    public IfaceState(DynActionForm form, String... items) {
        this.ifaceId = form.getParam(REQUEST_PARAM_IFACE_ID);
        this.state = items != null && items.length > 0 ? Utils.toString(Arrays.asList(items)) : form.getParam(REQUEST_PARAM_STATE);
    }

    public IfaceState(String objectType, int objectId, DynActionForm form, String... items) {
        this(form, items);
        this.objectType = objectType;
        this.objectId = objectId;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public int getObjectId() {
        return objectId;
    }

    public void setObjectId(int objectId) {
        this.objectId = objectId;
    }

    public String getIfaceId() {
        return ifaceId;
    }

    public void setIfaceId(String ifaceId) {
        this.ifaceId = ifaceId;
    }

    public String getState() {
        return state;
    }

    public void setState(String ifaceState) {
        this.state = ifaceState;
    }

    /**
     * @return state values separated with '/'
     */
    @Dynamic
    public String getFormattedState() {
        String result = "";
        if (state != null) {
            result = " " + Utils.toString(Utils.toList(state), "", "/");
        }
        return result;
    }
}
