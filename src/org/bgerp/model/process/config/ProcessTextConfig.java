package org.bgerp.model.process.config;

import org.bgerp.app.cfg.Config;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.dao.expression.ProcessExpressionObject;
import org.bgerp.dao.expression.ProcessLinkExpressionObject;
import org.bgerp.dao.expression.ProcessParamExpressionObject;

import ru.bgcrm.event.process.ProcessChangedEvent;
import ru.bgcrm.util.Utils;

abstract class ProcessTextConfig extends Config {
    private final String expression;

    ProcessTextConfig(ConfigMap config) throws InitStopException {
        super(null);

        expression = config.get(key());
        initWhen(Utils.notBlankString(expression));
    }

    protected abstract String key();

    public String getExpression() {
        return expression;
    }

    public boolean isProcessUsed(ProcessChangedEvent event) {
        return ProcessExpressionObject.called(expression, event);
    }

    public boolean isProcessLinkUsed() {
        return ProcessLinkExpressionObject.called(expression);
    }

    /**
     * Checks is a parameter value accessed
     * @param paramId the parameter or {@code 0} for checking access to any parameter value
     * @return is a parameter value accessed
     */
    public boolean isParamUsed(int paramId) {
        return ProcessParamExpressionObject.called(expression, paramId);
    }

}
