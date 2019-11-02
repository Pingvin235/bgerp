package ru.bgcrm.plugin.dispatch;

import ru.bgcrm.util.MailConfig;
import ru.bgcrm.util.ParameterMap;

public class Config extends ru.bgcrm.util.Config {
    private final MailConfig mailConfig;

    public Config(ParameterMap setup) {
        super(setup);
        setup = setup.sub("dispatch:manageEmail.");
        mailConfig = new MailConfig(setup);
    }

    public MailConfig getMailConfig() {
        return mailConfig;
    }
}
