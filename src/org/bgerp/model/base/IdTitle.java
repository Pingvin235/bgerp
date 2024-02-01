package org.bgerp.model.base;

import org.w3c.dom.Element;

import ru.bgcrm.util.Utils;

public class IdTitle extends Id implements org.bgerp.model.base.iface.IdTitle<Integer> {
    protected String title;

    @Override
    public String getTitle() {
        return title;
    }

    public void setTitle(String value) {
        this.title = value;
    }

    public IdTitle() {}

    public IdTitle(int id, String title) {
        this.id = id;
        this.title = title;
    }

    public IdTitle(Element element) {
        this(Utils.parseInt(element.getAttribute("id")), element.getAttribute("title"));
    }

    @Override
    public String toString() {
        return title != null ? title : "null";
    }
}
