package ru.bgcrm.model.process.wizard;

import org.apache.log4j.Logger;

import ru.bgcrm.dao.expression.Expression;
import ru.bgcrm.dynamic.DynamicClassManager;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.IdTitle;
import ru.bgcrm.util.ParameterMap;

public abstract class Step extends IdTitle {
	private static final Logger log = Logger.getLogger(Step.class);

	protected ParameterMap config;
	private String expression;

	public Step(ParameterMap config) {
		super(config.getInt("id", 0), config.get("title"));
		this.config = config;
		this.expression = config.get(Expression.CHECK_EXPRESSION_CONFIG_KEY);
	}

	public ParameterMap getConfig() {
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

	public static final Step newInstance(String className, ParameterMap config) {
		try {
			Class<?> clazz = DynamicClassManager.getClass(className);
			if (!Step.class.isAssignableFrom(clazz)) {
				throw new BGException("Incorrect class: " + className);
			}

			return (Step) clazz.getConstructor(ParameterMap.class).newInstance(config);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		return null;
	}
}