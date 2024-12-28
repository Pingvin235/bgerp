package ru.bgcrm.model.process.wizard;

import java.sql.Connection;

import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.cfg.bean.annotation.Bean;

import ru.bgcrm.model.process.wizard.base.Step;
import ru.bgcrm.model.process.wizard.base.StepData;
import ru.bgcrm.model.process.wizard.base.WizardData;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;

@Bean
public class SetDescriptionStep extends Step {
    public SetDescriptionStep(ConfigMap config) {
        super(config);
    }

    @Override
    public String getJsp() {
        return PATH_JSP + "/step_set_description.jsp";
    }

    @Override
    public Data data(WizardData data) {
        return new Data(this, data);
    }

    public static class Data extends StepData<SetDescriptionStep> {
        private Data(SetDescriptionStep step, WizardData data) {
            super(step, data);
        }

        @Override
        public boolean isFilled(DynActionForm form, Connection con) {
            return Utils.notBlankString(data.getProcess().getDescription());
        }
    }
}
