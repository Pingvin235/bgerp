package ru.bgcrm.model;

import org.w3c.dom.Element;

import ru.bgcrm.util.Utils;

public class IdTitle extends Id {
    protected String title;

    public String getTitle() {
        return title;
    }

    public void setTitle(String value) {
        this.title = value;
    }

    public IdTitle() {
    }

    public IdTitle(int id, String title) {
        this.id = id;
        this.title = title;
    }

    public IdTitle(Element element) {
        this(Utils.parseInt(element.getAttribute("id")), element.getAttribute("title"));
    }

    public String toString() {
        return title != null ? title : "null";
    }
}
