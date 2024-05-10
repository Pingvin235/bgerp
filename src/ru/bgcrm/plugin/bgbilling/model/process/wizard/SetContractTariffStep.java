package ru.bgcrm.plugin.bgbilling.model.process.wizard;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.bgerp.app.bean.annotation.Bean;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.model.base.IdTitle;

import ru.bgcrm.dao.process.ProcessLinkDAO;
import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.process.wizard.base.StepData;
import ru.bgcrm.model.process.wizard.base.WizardData;
import ru.bgcrm.plugin.bgbilling.proto.dao.ContractTariffDAO;
import ru.bgcrm.plugin.bgbilling.proto.model.Contract;
import ru.bgcrm.plugin.bgbilling.proto.model.tariff.ContractTariff;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;

@Bean
public class SetContractTariffStep extends BaseStep {
    public SetContractTariffStep(ConfigMap config) {
        super(config);
    }

    @Override
    public String getJsp() {
        return PATH_JSP + "/step_set_contract_tariff.jsp";
    }

    @Override
    public StepData<?> data(WizardData data) {
        return new Data(this, data);
    }

    public static class Data extends StepData<SetContractTariffStep> {
        private Contract contract;
        private List<IdTitle> tariffList;
        private ContractTariff contractTariff;

        private Data(SetContractTariffStep step, WizardData data) {
            super(step, data);
        }

        public Contract getContract() {
            return contract;
        }

        public List<IdTitle> getTariffList() {
            return tariffList;
        }

        public ContractTariff getContractTariff() {
            return contractTariff;
        }

        @Override
        public boolean isFilled(DynActionForm form, Connection con) {
            CommonObjectLink contractLink = Utils.getFirst(new ProcessLinkDAO(con).getObjectLinksWithType(data.getProcess().getId(), Contract.OBJECT_TYPE + "%"));
            if (contractLink == null)
                return false;

            contract = new Contract(contractLink);

            ContractTariffDAO tariffDao = new ContractTariffDAO(form.getUser(), contract.getBillingId());
            contractTariff = Utils.getFirst(tariffDao.contractTariffList(contract.getId()));
            tariffDao.getContractTariffPlan(-1, -1, contract.getId(), true, true, true, tariffList = new ArrayList<>());

            return contractTariff != null;
        }
    }
}
