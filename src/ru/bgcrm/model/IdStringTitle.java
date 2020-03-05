package ru.bgcrm.model;

public class IdStringTitle implements Title {
    private String id;
    private String title;
    
    public IdStringTitle() {}

    public IdStringTitle(String id, String title) {
        this.id = id;
        this.title = title;
    }
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return title != null ? title : "null";
    }
}
