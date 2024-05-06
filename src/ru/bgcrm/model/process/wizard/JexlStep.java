package ru.bgcrm.model.process.wizard;

import org.bgerp.app.bean.annotation.Bean;
import org.bgerp.app.cfg.ConfigMap;

import ru.bgcrm.dao.expression.Expression;

@Bean
public class JexlStep extends Step {
    private final String doExpression;

    public JexlStep(ConfigMap config) {
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
