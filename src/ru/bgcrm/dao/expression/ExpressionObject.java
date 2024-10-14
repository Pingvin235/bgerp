package ru.bgcrm.dao.expression;

import java.util.Map;

public interface ExpressionObject {
    public void toContext(Map<String, Object> context);
}
