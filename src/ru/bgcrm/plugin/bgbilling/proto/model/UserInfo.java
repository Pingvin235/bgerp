package ru.bgcrm.plugin.bgbilling.proto.model;

import org.bgerp.model.base.IdTitle;

public class UserInfo  {
    private int id;
    private String name;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
