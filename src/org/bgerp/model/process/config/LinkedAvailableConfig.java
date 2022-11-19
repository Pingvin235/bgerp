package org.bgerp.model.process.config;

import ru.bgcrm.util.ParameterMap;

public class LinkedAvailableConfig extends CommonAvailableConfig {
    protected LinkedAvailableConfig(ParameterMap config) throws InitStopException {
        super(config);
    }

    @Override
    protected String prefix() {
        return "linked";
    }
}
