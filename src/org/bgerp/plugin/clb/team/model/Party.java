package org.bgerp.plugin.clb.team.model;

import ru.bgcrm.model.IdTitle;

public class Party extends IdTitle {
    private String secret;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }
}
