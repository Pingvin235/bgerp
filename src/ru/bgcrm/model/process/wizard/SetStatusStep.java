package ru.bgcrm.model.process.wizard;

import org.bgerp.app.cfg.ConfigMap;

public class SetStatusStep extends Step {
    public SetStatusStep(ConfigMap config) {
        super(config);
    }

    @Override
    public String getJspFile() {
        return "/WEB-INF/jspf/usermob/process/process/wizard/step_set_status.jsp";
    }

    @Override
    public SetStatusStepData newStepData(WizardData data) {
        return new SetStatusStepData(this, data);
    }
}