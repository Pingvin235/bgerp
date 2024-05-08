package ru.bgcrm.plugin.bgbilling.model.process.wizard;

import java.sql.Connection;
import java.util.List;

import org.bgerp.app.bean.annotation.Bean;
import org.bgerp.app.cfg.ConfigMap;

import ru.bgcrm.dao.process.ProcessLinkDAO;
import ru.bgcrm.model.CommonObjectLink;
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
        private List<CommonObjectLink> contractLinkList;

        private Data(FindContractStep step, WizardData data) {
            super(step, data);
        }

        @Override
        public boolean isFilled(DynActionForm form, Connection con) {
            if (contractLinkList == null) {
                ProcessLinkDAO linkDao = new ProcessLinkDAO(con);
                contractLinkList = linkDao.getObjectLinksWithType(data.getProcess().getId(), Contract.OBJECT_TYPE + "%");
            }

            return contractLinkList.size() == 1;
        }

        public String getBillingId() {
            return step.getBillingId();
        }
    }
}
