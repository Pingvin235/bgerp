package ru.bgcrm.model.process.wizard;

import java.sql.Connection;
import java.util.Map;

import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.cfg.bean.annotation.Bean;
import org.bgerp.dao.expression.Expression;

import ru.bgcrm.model.process.wizard.base.Step;
import ru.bgcrm.model.process.wizard.base.StepData;
import ru.bgcrm.model.process.wizard.base.WizardData;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.sql.SingleConnectionSet;

@Bean
public class JexlStep extends Step {
    private final String doExpression;

    public JexlStep(ConfigMap config) {
        super(config);
        doExpression = config.get(Expression.DO_EXPRESSION_CONFIG_KEY, "");
    }

    public String getDoExpression() {
        return doExpression;
    }

    @Override
    public String getJsp() {
        return PATH_JSP + "/step_jexl.jsp";
    }

    @Override
    public Data data(WizardData data) {
        return new Data(this, data);
    }

    public static class Data extends StepData<JexlStep> {
        private boolean filled = false;
        private String message;

        private Data(JexlStep step, WizardData data) {
            super(step, data);
        }

        @Override
        public boolean isFilled(DynActionForm form, Connection con) {
            Map<String, Object> context = Expression.context(new SingleConnectionSet(con), form, null, data.getProcess());
            Map<?, ?> state = (Map<?, ?>) new Expression(context).execute(step.getDoExpression());
            if (state == null)
                return filled = false;

            filled = (Boolean) state.get("filled");
            message = (String) state.get("message");

            return filled;
        }

        public boolean isFilled() {
            return filled;
        }

        public String getMessage() {
            return message;
        }
    }
}
