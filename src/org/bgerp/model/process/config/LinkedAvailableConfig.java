package org.bgerp.model.process.config;

import org.bgerp.app.cfg.ConfigMap;

public class LinkedAvailableConfig extends CommonAvailableConfig {
    protected LinkedAvailableConfig(ConfigMap config) throws InitStopException {
        super(config);
    }

    @Override
    protected String prefix() {
        return "linked";
    }
}
