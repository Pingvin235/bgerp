package org.bgerp.model.process.config;

import org.bgerp.app.cfg.ConfigMap;

import ru.bgcrm.dao.expression.Expression;

public class ProcessTitleConfig extends ProcessTextConfig {
    private static final String KEY = "title." + Expression.EXPRESSION_CONFIG_KEY;

    protected ProcessTitleConfig(ConfigMap config) throws InitStopException {
        super(config);
    }

    @Override
    protected String key() {
        return KEY;
    }
}
