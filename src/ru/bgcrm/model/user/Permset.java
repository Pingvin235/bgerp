package ru.bgcrm.model.user;

import org.bgerp.model.base.IdTitle;

import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Preferences;

public class Permset extends IdTitle {
    private String roles = "";
    private String comment;
    private String config;
    private ParameterMap configMap = new Preferences();

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String description) {
        this.comment = description;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
        this.configMap = new Preferences(config);
    }

    public ParameterMap getConfigMap() {
        return configMap;
    }
}
