package ru.bgcrm.model;

public class IdTitleComment extends IdTitle {
    private String comment = "";

    public IdTitleComment() {
    }

    public IdTitleComment(int id, String title, String comment) {
        super(id, title);
        this.comment = comment;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
