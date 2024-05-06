package ru.bgcrm.model.process.wizard;

import org.bgerp.app.bean.annotation.Bean;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.cache.ParameterCache;
import org.bgerp.model.param.Parameter;
import org.bgerp.util.Log;

@Bean
public class ContinueStep extends Step {
    private static final Log log = Log.getLog();

    private final Parameter param;

    public ContinueStep(ConfigMap config) {
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