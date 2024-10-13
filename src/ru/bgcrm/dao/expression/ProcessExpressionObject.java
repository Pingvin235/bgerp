package ru.bgcrm.dao.expression;

import ru.bgcrm.model.process.Process;
import java.util.Map;

public class ProcessExpressionObject extends Process {
    public static final String KEY = Process.OBJECT_TYPE;
    public static final String KEY_SHORT = "p";

    public static void context(Map<String, Object> context, Process process) {
        context.put(KEY, process);
        context.put(KEY_SHORT, process);
    }
}
