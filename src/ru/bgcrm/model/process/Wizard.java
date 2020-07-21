package ru.bgcrm.model.process;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.bgcrm.dao.expression.Expression;
import ru.bgcrm.dao.expression.ParamValueFunction;
import ru.bgcrm.model.process.wizard.Step;
import ru.bgcrm.servlet.filter.SetRequestParamsFilter;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Utils;

public class Wizard {
    // шаги, которые необходимо заполнить при создании процесса
    private final List<Step> createStepList = new ArrayList<Step>();

    // шаги, открываемы при доступе к карточке процесса
    private final List<Step> stepList = new ArrayList<Step>();

    // для кого отображать кнопку мастера
    private String expression;

    public Wizard(TypeProperties props) {
        ParameterMap configMap = props.getConfigMap();
        ParameterMap wizardConf = configMap.sub("createWizard.");

        // TODO: Оставлена возможность указания выражения в параметре конфигурации expression,
        // убрать в далёком будущем 19.09.2014
        this.expression = wizardConf.get(Expression.STRING_MAKE_EXPRESSION_CONFIG_KEY + "Show", configMap.get("expression"));

        loadSteps(wizardConf, "createStep.", createStepList);
        loadSteps(wizardConf, "step.", stepList);
    }

    protected void loadSteps(ParameterMap wizardConf, String prefix, List<Step> stepList) {
        for (Map.Entry<Integer, ParameterMap> me : wizardConf.subIndexed(prefix).entrySet()) {
            ParameterMap config = me.getValue();

            String className = config.get("class");

            Step step = Step.newInstance(className, config);
            if (step != null) {
                stepList.add(step);
            }
        }
    }

    public List<Step> getStepList() {
        return stepList;
    }

    public List<Step> getCreateStepList() {
        return createStepList;
    }

    public boolean check(Connection con, DynActionForm form, Process process) {
        if (Utils.isBlankString(expression)) {
            return true;
        }

        Map<String, Object> context = new HashMap<String, Object>();
        context.put(Process.OBJECT_TYPE, process);
        context.put(Process.OBJECT_TYPE + ParamValueFunction.PARAM_FUNCTION_SUFFIX, new ParamValueFunction(con, process.getId()));

        context.putAll(SetRequestParamsFilter.getContextVariables(form.getHttpRequest()));
        // TODO: Use DefaultProcessChangeListener#initExpression()
        return new Expression(context).check(expression);
    }
}