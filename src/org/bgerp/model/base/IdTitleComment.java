package org.bgerp.model.base;

import org.bgerp.model.base.iface.Comment;

public class IdTitleComment extends IdTitle implements Comment {
    private String comment = "";

    public IdTitleComment() {
    }

    public IdTitleComment(int id, String title, String comment) {
        super(id, title);
        this.comment = comment;
    }

    @Override
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
