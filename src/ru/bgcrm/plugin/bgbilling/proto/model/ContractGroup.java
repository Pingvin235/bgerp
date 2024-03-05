package ru.bgcrm.plugin.bgbilling.proto.model;

import org.bgerp.model.base.IdTitle;

public class ContractGroup extends IdTitle {
    private boolean enabled = true;
    private boolean editable = true;
    private String comment = null;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
