package org.bgerp.model.process.config;

import org.bgerp.app.cfg.ConfigMap;

public class LinkAvailableConfig extends CommonAvailableConfig {
    protected LinkAvailableConfig(ConfigMap config) throws InitStopException {
        super(config);
    }

    @Override
    protected String prefix() {
        return "linked";
    }
}
