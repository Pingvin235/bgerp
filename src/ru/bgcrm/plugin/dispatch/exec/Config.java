package ru.bgcrm.plugin.dispatch.exec;

import ru.bgcrm.util.MailConfig;
import ru.bgcrm.util.ParameterMap;

public class Config extends ru.bgcrm.util.Config {
    private final MailConfig mailConfig;

    public Config(ParameterMap config) {
        super(null);
        config = config.sub("dispatch:manageEmail.");
        mailConfig = new MailConfig(config);
    }

    public MailConfig getMailConfig() {
        return mailConfig;
    }
}
