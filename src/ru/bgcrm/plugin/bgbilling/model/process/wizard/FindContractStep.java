package ru.bgcrm.plugin.bgbilling.model.process.wizard;

import java.sql.Connection;

import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.cfg.bean.annotation.Bean;

import ru.bgcrm.dao.process.ProcessLinkDAO;
import ru.bgcrm.model.process.wizard.base.StepData;
import ru.bgcrm.model.process.wizard.base.WizardData;
import ru.bgcrm.plugin.bgbilling.proto.model.Contract;
import ru.bgcrm.struts.form.DynActionForm;

@Bean
public class FindContractStep extends BaseStep {
    private final String billingId;

    public FindContractStep(ConfigMap config) {
        super(config);
        billingId = config.get("billingId");
    }

    public String getBillingId() {
        return billingId;
    }

    @Override
    public String getJsp() {
        return PATH_JSP + "/step_find_contract.jsp";
    }

    @Override
    public StepData<?> data(WizardData data) {
        return new Data(this, data);
    }

    public static class Data extends StepData<FindContractStep> {
        private Data(FindContractStep step, WizardData data) {
            super(step, data);
        }

        @Override
        public boolean isFilled(DynActionForm form, Connection con) {
            return new ProcessLinkDAO(con).getObjectLinksWithType(data.getProcess().getId(), Contract.OBJECT_TYPE + "%").size() == 1;
        }
    }
}
