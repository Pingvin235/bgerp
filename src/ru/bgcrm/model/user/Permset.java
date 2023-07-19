package ru.bgcrm.model.user;

import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.cfg.Preferences;
import org.bgerp.model.base.IdTitle;

public class Permset extends IdTitle {
    private String roles = "";
    private String comment;
    private String config;
    private ConfigMap configMap = new Preferences();

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

    public ConfigMap getConfigMap() {
        return configMap;
    }
}
