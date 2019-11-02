package ru.bgcrm.model;

public class Counter {
    public Counter() {
        id = -1;
        value = -1;
    }

    private int id;
    private String title;
    private int value;
    private String prefix;
    private String postfix;

    public String getPostfix() {
        return postfix;
    }

    public void setPostfix(String postfix) {
        this.postfix = postfix;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
