package org.bgerp.model.process.config;

import ru.bgcrm.util.ParameterMap;

public class LinkAvailableConfig extends CommonAvailableConfig {
    protected LinkAvailableConfig(ParameterMap config) throws InitStopException {
        super(config);
    }

    @Override
    protected String prefix() {
        return "linked";
    }
}
