package ru.bgcrm.plugin.bgbilling.proto.model;

import org.bgerp.model.base.Id;

public class UserInfo extends Id {
    private String name;

    public UserInfo() {
    }

    public UserInfo(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
