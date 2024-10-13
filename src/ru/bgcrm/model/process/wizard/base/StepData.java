package ru.bgcrm.model.process.wizard.base;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import ru.bgcrm.dao.expression.Expression;
import ru.bgcrm.dao.expression.ParamExpressionObject;
import ru.bgcrm.dao.expression.ProcessLinkExpressionObject;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.user.User;
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
        context.put(Process.OBJECT_TYPE, data.getProcess());
        context.put(Process.OBJECT_TYPE + ParamExpressionObject.PARAM_FUNCTION_SUFFIX,
                new ParamExpressionObject(con, data.getProcess().getId()));
        context.put(ProcessLinkExpressionObject.KEY, new ProcessLinkExpressionObject(con, data.getProcess().getId()));
        context.put(User.OBJECT_TYPE, data.getUser());

        return new Expression(context).check(step.getExpression());
    }

    public abstract boolean isFilled(DynActionForm form, Connection con) throws Exception;
}
