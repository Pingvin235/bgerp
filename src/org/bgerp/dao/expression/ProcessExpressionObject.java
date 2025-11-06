package org.bgerp.dao.expression;

import java.util.Map;

import ru.bgcrm.model.process.Process;
import ru.bgcrm.util.Utils;

/**
 * Expression object for accessing {@link Process}
 *
 * @author Shamil Vakhitov
 */
public class ProcessExpressionObject implements ExpressionObject {
    public static final String KEY = Process.OBJECT_TYPE;
    private static final String KEY_SHORT = "p";

    public static final boolean called(String expression) {
        return Utils.notBlankString(expression) && (expression.contains(KEY_SHORT + ".") || expression.contains(KEY + "."));
    }

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
