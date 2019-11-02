package ru.bgcrm.model.process.wizard;

import ru.bgcrm.dao.expression.Expression;
import ru.bgcrm.util.ParameterMap;

public class JexlStep extends Step {
	private final String doExpression;
	
	public JexlStep(ParameterMap config) {
		super(config);
		doExpression = config.get(Expression.DO_EXPRESSION_CONFIG_KEY, "");
	}
	
	@Override
	public String getJspFile() {
		return "/WEB-INF/jspf/usermob/process/process/wizard/step_jexl.jsp";
	}

	@Override
	public JexlStepData newStepData(WizardData data) {
		return new JexlStepData(this, data);
	}

	public String getDoExpression() {
		return doExpression;
	}
}
