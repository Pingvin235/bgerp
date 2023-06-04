package ru.bgcrm.plugin.task.model;

import org.bgerp.model.base.IdStringTitle;

import ru.bgcrm.dao.expression.Expression;
import ru.bgcrm.util.ParameterMap;

public class TaskType extends IdStringTitle {

    private final String doExpression;

    public TaskType(ParameterMap config) {
        super(config.get("id"), config.get("title"));
        doExpression = config.get(Expression.DO_EXPRESSION_CONFIG_KEY);
    }

    public String getDoExpression() {
        return doExpression;
    }

}
