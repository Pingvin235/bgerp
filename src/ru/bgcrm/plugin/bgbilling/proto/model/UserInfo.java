package ru.bgcrm.plugin.bgbilling.proto.model;

import org.bgerp.model.base.Id;

/**
 * The class is used to represent data both from the billing's UserInfo, the {@code name} field, and from User, the {@code login} field, which is not yet present in the billing's UserInfo
 */
public class UserInfo extends Id {
    private String login;
    private String name;

    public UserInfo() {
    }

    public UserInfo(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
