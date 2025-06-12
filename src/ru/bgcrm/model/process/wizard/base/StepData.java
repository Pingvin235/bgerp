package ru.bgcrm.model.process.wizard.base;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import org.bgerp.dao.expression.Expression;
import org.bgerp.dao.expression.ProcessExpressionObject;
import org.bgerp.dao.expression.ProcessLinkExpressionObject;
import org.bgerp.dao.expression.ProcessParamExpressionObject;
import org.bgerp.dao.expression.UserExpressionObject;

import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;

public abstract class StepData<T extends Step> {
    protected final T step;
    protected final WizardData data;

    protected StepData(T step, WizardData data) {
        this.step = step;
        this.data = data;
    }

    public T getStep() {
        return step;
    }

    /**
     * JEXL фильтр позволяющий включить или выключить шаг из списка.
     * @param con
     * @return
     */
    public boolean check(Connection con) {
        if (Utils.isBlankString(step.getExpression())) {
            return true;
        }

        Map<String, Object> context = new HashMap<>();
        new ProcessExpressionObject(data.getProcess()).toContext(context);
        new ProcessParamExpressionObject(con, data.getProcess().getId()).toContext(context);
        new ProcessLinkExpressionObject(con, data.getProcess().getId()).toContext(context);
        new UserExpressionObject(data.getUser()).toContext(context);

        return new Expression(context).executeCheck(step.getExpression());
    }

    public abstract boolean isFilled(DynActionForm form, Connection con) throws Exception;
}
