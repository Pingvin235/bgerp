package ru.bgcrm.model.process.wizard;

import org.bgerp.app.bean.annotation.Bean;
import org.bgerp.app.cfg.ConfigMap;

@Bean
public class SetExecutorsStep extends Step {
    public SetExecutorsStep(ConfigMap config) {
        super(config);
    }

    @Override
    public String getJspFile() {
        return "/WEB-INF/jspf/usermob/process/process/wizard/step_set_executors.jsp";
    }

    @Override
    public SetExecutorsStepData newStepData(WizardData data) {
        return new SetExecutorsStepData(this, data);
    }
}