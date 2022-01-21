package ru.bgcrm.model.process.wizard;

import java.sql.Connection;
import java.util.Map;

import ru.bgcrm.dao.expression.Expression;
import ru.bgcrm.event.listener.DefaultProcessChangeListener;
import ru.bgcrm.model.BGException;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.sql.SingleConnectionSet;

public class JexlStepData extends StepData<JexlStep> {
    private boolean filled = false;
    private String message;

    public JexlStepData(JexlStep step, WizardData data) {
        super(step, data);
    }

    @Override
    public boolean isFilled(DynActionForm form, Connection con) throws BGException {
        Map<String, Object> context = DefaultProcessChangeListener.getProcessJexlContext(
                new SingleConnectionSet(con), form, null, data.getProcess());
        Map<?, ?> state = (Map<?, ?>) new Expression(context).executeScript(step.getDoExpression());
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
