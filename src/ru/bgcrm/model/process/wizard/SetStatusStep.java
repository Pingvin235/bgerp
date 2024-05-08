package ru.bgcrm.model.process.wizard;

import java.sql.Connection;

import org.bgerp.app.bean.annotation.Bean;
import org.bgerp.app.cfg.ConfigMap;

import ru.bgcrm.model.process.wizard.base.Step;
import ru.bgcrm.model.process.wizard.base.StepData;
import ru.bgcrm.model.process.wizard.base.WizardData;
import ru.bgcrm.struts.form.DynActionForm;

@Bean
public class SetStatusStep extends Step {
    public SetStatusStep(ConfigMap config) {
        super(config);
    }

    @Override
    public String getJsp() {
        return PATH_JSP + "/step_set_status.jsp";
    }

    @Override
    public Data data(WizardData data) {
        return new Data(this, data);
    }

    public static class Data extends StepData<SetStatusStep> {
        private Data(SetStatusStep step, WizardData data) {
            super(step, data);
        }

        @Override
        public boolean isFilled(DynActionForm form, Connection con) {
            return true;
        }
    }
}