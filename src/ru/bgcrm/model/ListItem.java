package ru.bgcrm.model;

public class ListItem {
    private int id = -1;
    private String key;
    private String title;

    public ListItem() {
    }

    public ListItem(int id, String title) {
        this.id = id;
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        String result = title;
        if (title == null) {
            result = super.toString();
        }
        return result;
    }
}
