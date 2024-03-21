package ru.bgcrm.model.process.wizard;

import org.bgerp.app.bean.Bean;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.exception.BGException;
import org.bgerp.model.base.IdTitle;
import org.bgerp.util.Log;

import ru.bgcrm.dao.expression.Expression;

public abstract class Step extends IdTitle {
    private static final Log log = Log.getLog();

    protected ConfigMap config;
    private String expression;

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

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public abstract String getJspFile();

    public abstract StepData<?> newStepData(WizardData data);

    public static final Step newInstance(String className, ConfigMap config) {
        try {
            Class<?> clazz = Bean.getClass(className);
            if (!Step.class.isAssignableFrom(clazz)) {
                throw new BGException("Incorrect class: " + className);
            }

            return (Step) clazz.getConstructor(ConfigMap.class).newInstance(config);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return null;
    }
}