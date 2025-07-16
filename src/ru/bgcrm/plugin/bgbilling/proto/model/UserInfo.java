package ru.bgcrm.plugin.bgbilling.proto.model;

import org.bgerp.model.base.Id;

/**
 * Класс используется для представления данных как из UserInfo биллинга, поле {@code name}.
 * Так и User, поле {@code login}, которое пока отсутствует в биллинговском UserInfo.
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
