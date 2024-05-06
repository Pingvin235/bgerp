package ru.bgcrm.model.process.wizard;

import org.bgerp.app.bean.annotation.Bean;
import org.bgerp.app.cfg.ConfigMap;

@Bean
public class SetDescriptionStep extends Step {
    public SetDescriptionStep(ConfigMap config) {
        super(config);
    }

    @Override
    public String getJspFile() {
        return "/WEB-INF/jspf/usermob/process/process/wizard/step_set_description.jsp";
    }

    @Override
    public SetDescriptionStepData newStepData(WizardData data) {
        return new SetDescriptionStepData(this, data);
    }

}
