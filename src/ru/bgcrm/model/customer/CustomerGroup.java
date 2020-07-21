package ru.bgcrm.model.customer;

import ru.bgcrm.model.IdTitle;

// TODO: Is not really used, check and either activate or delete.
public class CustomerGroup extends IdTitle {
    private String comment;

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
