package ru.bgcrm.dao.expression;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.jexl3.JexlArithmetic;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlException;
import org.apache.commons.jexl3.MapContext;
import org.apache.commons.lang3.StringUtils;
import org.bgerp.app.event.EventProcessor;
import org.bgerp.app.event.iface.Event;
import org.bgerp.event.base.UserEvent;
import org.bgerp.util.Log;
import org.bgerp.util.TimeConvert;

import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.user.User;
import ru.bgcrm.servlet.filter.SetRequestParamsFilter;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

/**
 * JEXL expression processor.
 * JEXL scripts are used for condition checking and execution small scripts.
 *
 * http://commons.apache.org/jexl/reference/syntax.html#Functions
 */
public class Expression {
    private static final Log log = Log.getLog();

    public static final String EXPRESSION_CONFIG_KEY = "expression";
    @Deprecated
    public static final String STRING_MAKE_EXPRESSION_CONFIG_KEY = "stringExpression";
    // TODO: Change the keys to: "check.expression", "check.error.message", "do.expression"
    public static final String CHECK_EXPRESSION_CONFIG_KEY = "checkExpression";
    public static final String CHECK_ERROR_MESSAGE_CONFIG_KEY = "checkErrorMessage";
    public static final String DO_EXPRESSION_CONFIG_KEY = "doExpression";

    private static final Utils PREFIX_u = new Utils();
    private static final TimeUtils PREFIX_tu = new TimeUtils();
    private static final TimeConvert PREFIX_tc = new TimeConvert();
    private static final StringUtils PREFIX_su = new StringUtils();
    private static final CollectionUtils PREFIX_cu = new CollectionUtils();
    @SuppressWarnings("deprecation")
    private static final FileUtils PREFIX_fu = new FileUtils();

    private JexlEngine jexl;

    // Вроде как тут заложена возможность создавать классы по их полному названию,
    // скорее всего для вызовов статических методов вида ru.bgcrm.Utils.doSomth().
    private JexlContext context = new MapContext() {
        @Override
        public boolean has(String name) {
            try {
                return super.has(name) || Class.forName(name) != null;
            } catch (ClassNotFoundException xnf) {
                return false;
            }
        }

        @Override
        public Object get(String name) {
            try {
                Object found = super.get(name);
                if (found == null && !super.has(name)) {
                    found = Class.forName(name);
                }
                return found;
            } catch (ClassNotFoundException xnf) {
                return null;
            }
        }
    };

    public static final class ContextInitEvent implements Event {
        private final Map<String, Object> context;

        public ContextInitEvent(Map<String, Object> context) {
            this.context = context;
        }

        public Map<String, Object> getContext() {
            return context;
        }
    }

    public Expression(Map<String, Object> contextVars) {
        // TreeMap would be better here, but doesn't support null keys
        contextVars = new HashMap<>(contextVars);

        try {
            EventProcessor.processEvent(new ContextInitEvent(contextVars), null);
        } catch (Exception e) {
            log.error(e);
        }

        JexlBuilder jexlBuilder = new JexlBuilder()
                .namespaces(contextVars) // нам нужен только неймспейс null
                .strict(true) // выброс исключений
                .arithmetic(new JexlArithmetic(false)); // но игнорирование null операндов
        jexl = jexlBuilder.create();

        //TODO: Для совместимости пока объекты декларируются и как объекты и как функции.
        //В будущем оставить только объекты, методы вызывать через точку.

        setExpressionContextUtils(contextVars);

        contextVars.put("log", log);

        contextVars.put("NEW_LINE", "\n");
        contextVars.put("NEW_LINE2", "\n\n");

        // все функции объявляются как переменные контекста
        contextVars.entrySet().stream().filter(me -> me.getKey() != null)
                .forEach(me -> context.set(me.getKey(), me.getValue()));

        // установка ссылки на экспрешшен
        contextVars.values().stream().filter(v -> v instanceof ExpressionContextAccessingObject)
                .forEach(v -> ((ExpressionContextAccessingObject) v).setExpression(this));
    }

    public static void setExpressionContextUtils(Map<String, Object> contextVars) {
        contextVars.put("u", PREFIX_u);
        contextVars.put("tu", PREFIX_tu);
        contextVars.put("tc", PREFIX_tc);
        contextVars.put("su", PREFIX_su);
        contextVars.put("cu", PREFIX_cu);
        contextVars.put("fu", PREFIX_fu);
    }

    public Object getContextObject(String name) {
        return context.get(name);
    }

    public boolean check(String expression) {
        return (Boolean) jexl.createScript(expression).execute(context);
    }

    public String getString(String expression) {
        return (String) jexl.createScript(expression).execute(context);
    }

    public Object executeScript(String expression) {
        log.debug("Executing script: {}", expression);

        try {
            return jexl.createScript(expression).execute(context);
        } catch (JexlException e) {
            int lineNumber = Utils.parseInt(StringUtils.substringBetween(e.getMessage(), "@", ":"));
            String[] lines = expression.split("\\n");
            if (lineNumber > 0 && lineNumber <= lines.length)
                log.error("INCORRECT SCRIPT LINE: " + lines[lineNumber - 1]);
            throw e;
        }
    }

    /**
     * Creates initialized expression for a process related expression.
     * @param conSet DB connection sets.
     * @param event event.
     * @param process the process.
     * @return
     * @throws Exception
     */
    public static Expression init(ConnectionSet conSet, UserEvent event, Process process) throws Exception {
        DynActionForm form = event.getForm();
        Map<String, Object> context = context(conSet, form, event, process);
        return new Expression(context);
    }

    /**
     * Creates expressions' context for process related expression.
     * @param conSet DB connections set.
     * @param form request form.
     * @param event event, can be {@code null}.
     * @param process the process.
     * @return
     */
    public static Map<String, Object> context(ConnectionSet conSet, DynActionForm form, UserEvent event, Process process) {
        Connection con = conSet.getConnection();

        Map<String, Object> context = new HashMap<>(100);
        context.put(User.OBJECT_TYPE, form.getUser());
        context.put(User.OBJECT_TYPE + ParamValueFunction.PARAM_FUNCTION_SUFFIX, new ParamValueFunction(con, form.getUserId()));
        context.put(Process.OBJECT_TYPE, process);
        context.put(Process.OBJECT_TYPE + ParamValueFunction.PARAM_FUNCTION_SUFFIX, new ParamValueFunction(con, process.getId()));
        context.put(ProcessLinkFunction.PROCESS_LINK_FUNCTION, new ProcessLinkFunction(con, process.getId()));
        context.put(ConnectionSet.KEY, conSet);
        context.put(DynActionForm.KEY, form);
        if (event != null)
            context.put(Event.KEY, event);

        context.put(null, new ProcessChangeFunctions(process, form, con));

        context.putAll(SetRequestParamsFilter.getContextVariables(form.getHttpRequest()));

        return context;
    }
}