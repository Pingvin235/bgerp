package ru.bgcrm.plugin.task.model;

import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.model.base.IdStringTitle;

import ru.bgcrm.dao.expression.Expression;

public class TaskType extends IdStringTitle {

    private final String doExpression;

    public TaskType(ConfigMap config) {
        super(config.get("id"), config.get("title"));
        doExpression = config.get(Expression.DO_EXPRESSION_CONFIG_KEY);
    }

    public String getDoExpression() {
        return doExpression;
    }

}
