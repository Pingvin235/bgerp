package ru.bgcrm.model.process.wizard;

import org.bgerp.util.Log;

import ru.bgcrm.cache.ParameterCache;
import ru.bgcrm.model.param.Parameter;
import ru.bgcrm.util.ParameterMap;

public class ContinueStep extends Step {
    private static final Log log = Log.getLog();

    private final Parameter param;

    public ContinueStep(ParameterMap config) {
        super(config);

        int paramId = config.getInt("parameterId", 0);
        param = ParameterCache.getParameter(paramId);
        if (param == null) {
            log.error("Not found param: " + paramId);
        }
    }

    @Override
    public String getJspFile() {
        return "/WEB-INF/jspf/usermob/process/process/wizard/step_continue.jsp";
    }

    @Override
    public StepData<?> newStepData(WizardData data) {
        return new ContinueStepData(this, data);
    }

    public Parameter getParam() {
        return param;
    }
}