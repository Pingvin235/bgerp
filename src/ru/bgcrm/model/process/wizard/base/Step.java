package ru.bgcrm.model.process.wizard.base;

import org.bgerp.action.base.BaseAction;
import org.bgerp.app.bean.Bean;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.exception.BGException;
import org.bgerp.app.servlet.jsp.GetJsp;
import org.bgerp.model.base.IdTitle;
import org.bgerp.util.Log;

import ru.bgcrm.dao.expression.Expression;

public abstract class Step extends IdTitle implements GetJsp {
    private static final Log log = Log.getLog();

    protected static final String PATH_JSP = BaseAction.PATH_JSP_USER + "/process/wizard";

    protected final ConfigMap config;
    private final String expression;

    public Step(ConfigMap config) {
        super(config.getInt("id", 0), config.get("title"));
        this.config = config;
        this.expression = config.get(Expression.CHECK_EXPRESSION_CONFIG_KEY);
    }

    public ConfigMap getConfig() {
        return config;
    }

    public String getExpression() {
        return expression;
    }

    public abstract StepData<?> data(WizardData data);

    public static final Step newInstance(String className, ConfigMap config) {
        try {
            Class<?> clazz = Bean.getClass(className);
            if (!Step.class.isAssignableFrom(clazz)) {
                throw new BGException("Incorrect class: " + className);
            }

            return (Step) clazz.getConstructor(ConfigMap.class).newInstance(config);
        } catch (Exception e) {
            log.error(e);
        }

        return null;
    }
}