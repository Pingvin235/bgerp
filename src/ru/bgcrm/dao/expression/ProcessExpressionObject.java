package ru.bgcrm.dao.expression;

import java.util.Map;

import ru.bgcrm.model.process.Process;

public class ProcessExpressionObject extends Process implements ExpressionObject {
    public static final String KEY = Process.OBJECT_TYPE;
    public static final String KEY_SHORT = "p";

    private final Process process;

    public ProcessExpressionObject(Process process) {
        this.process = process;
    }

    @Override
    public void toContext(Map<String, Object> context) {
        context.put(KEY, process);
        context.put(KEY_SHORT, process);
    }
}
