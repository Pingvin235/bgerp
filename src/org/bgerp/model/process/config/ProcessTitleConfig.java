package org.bgerp.model.process.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bgerp.app.cfg.Config;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.util.Log;

import ru.bgcrm.dao.expression.CalledParamIdsExpressionObject;
import ru.bgcrm.dao.expression.Expression;
import ru.bgcrm.dao.expression.ProcessExpressionObject;
import ru.bgcrm.util.Utils;

public class ProcessTitleConfig extends Config {
    private static final Log log = Log.getLog();

    private static final String KEY = "title." + Expression.EXPRESSION_CONFIG_KEY;

    private final String expression;
    private final Set<Integer> paramIds;

    protected ProcessTitleConfig(ConfigMap config) throws InitStopException {
        super(null);

        if (!config.subIndexed("processReference.").isEmpty())
            log.error("'processReference' configuration key isn't supported anymore, use '{}' instead", KEY);

        expression = config.get(KEY);
        initWhen(Utils.notBlankString(expression));
        paramIds = paramIds();
        initWhen(!paramIds.isEmpty());
    }

    public String getExpression() {
        return expression;
    }

    public boolean isProcessUsed() {
        return expression.contains(ProcessExpressionObject.KEY + ".") || expression.contains(ProcessExpressionObject.KEY_SHORT + ".");
    }

    public Set<Integer> getParamIds() {
        return paramIds;
    }

    private Set<Integer> paramIds() {
        var pp = new CalledParamIdsExpressionObject();

        Map<String, Object> context = new HashMap<>();
        pp.toContext(context);

        new Expression(context).executeGetString(expression);

        return pp.getParamIds();
    }
}
