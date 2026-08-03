package org.bgerp.dao.expression;

import java.util.Map;
import java.util.Set;

import org.bgerp.util.text.CallsFinder;

import ru.bgcrm.event.process.ProcessChangedEvent;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.util.Utils;

/**
 * Expression object for accessing {@link Process}
 *
 * @author Shamil Vakhitov
 */
public class ProcessExpressionObject implements ExpressionObject {
    private static final String KEY = Process.OBJECT_TYPE;
    private static final String KEY_SHORT = "p";

    public static final Process contextProcess(Map<String, Object> context) {
        return (Process) context.get(KEY);
    }

    public static final boolean called(String expression, ProcessChangedEvent event) {
        boolean result = Utils.notBlankString(expression) && (expression.contains(KEY_SHORT + ".") || expression.contains(KEY + "."));

        if (result && event != null) {
            CallsFinder finder = null;

            var variables = Set.of(KEY, KEY_SHORT);

            if (event.isStatus())
                finder = new CallsFinder(variables, Set.of("getStatus"));
            else if (event.isDescription())
                finder = new CallsFinder(variables, Set.of("getDescription"));
            else if (event.isGroups())
                finder = new CallsFinder(variables, Set.of("getGroup"));
            else if (event.isExecutors())
                finder = new CallsFinder(variables, Set.of("getExecutor"));
            else if (event.isPriority())
                finder = new CallsFinder(variables, Set.of("getPriority"));
            else if (event.isType())
                finder = new CallsFinder(variables, Set.of("getType"));

            if (finder != null)
                result = finder.find(expression);
        }

        return result;
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
