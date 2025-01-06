package ru.bgcrm.plugin.dispatch.exec;

import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.util.mail.MailConfig;

public class Config extends org.bgerp.app.cfg.Config {
    private final MailConfig mailConfig;

    public Config(ConfigMap config) {
        super(null);
        config = config.sub("dispatch:manageEmail.");
        mailConfig = new MailConfig(config);
    }

    public MailConfig getMailConfig() {
        return mailConfig;
    }
}
