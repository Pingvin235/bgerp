package org.bgerp.model.process.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bgerp.app.cfg.Config;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.dao.expression.CalledParamIdsExpressionObject;
import org.bgerp.dao.expression.Expression;
import org.bgerp.dao.expression.ProcessExpressionObject;

import ru.bgcrm.util.Utils;

abstract class ProcessTextConfig extends Config {
    private final String expression;
    private final Set<Integer> paramIds;

    ProcessTextConfig(ConfigMap config) throws InitStopException {
        super(null);

        expression = config.get(key());
        initWhen(Utils.notBlankString(expression));
        paramIds = paramIds();
    }

    protected abstract String key();

    public String getExpression() {
        return expression;
    }

    public boolean isProcessUsed() {
        return expression.contains(ProcessExpressionObject.KEY + ".") || expression.contains(ProcessExpressionObject.KEY_SHORT + ".");
    }

    public boolean isAnyParamUsed() {
        return !paramIds.isEmpty();
    }

    public boolean isParamUsed(int paramId) {
        return paramIds.contains(paramId);
    }

    private Set<Integer> paramIds() {
        var pp = new CalledParamIdsExpressionObject();

        Map<String, Object> context = new HashMap<>();
        pp.toContext(context);

        new Expression(context, true).executeGetString(expression);

        return pp.getParamIds();
    }
}
