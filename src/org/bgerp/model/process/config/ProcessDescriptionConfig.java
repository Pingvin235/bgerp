package org.bgerp.model.process.config;

import org.bgerp.app.cfg.ConfigMap;

import ru.bgcrm.dao.expression.Expression;

public class ProcessDescriptionConfig extends ProcessTextConfig {
    private static final String KEY = "description." + Expression.EXPRESSION_CONFIG_KEY;

    protected ProcessDescriptionConfig(ConfigMap config) throws InitStopException {
        super(config);
    }

    @Override
    protected String key() {
        return KEY;
    }
}
