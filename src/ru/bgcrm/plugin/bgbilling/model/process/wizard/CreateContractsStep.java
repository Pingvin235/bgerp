package ru.bgcrm.plugin.bgbilling.model.process.wizard;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.cfg.bean.annotation.Bean;

import ru.bgcrm.dao.process.ProcessLinkDAO;
import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.customer.Customer;
import ru.bgcrm.model.process.wizard.LinkCustomerStep;
import ru.bgcrm.model.process.wizard.base.StepData;
import ru.bgcrm.model.process.wizard.base.WizardData;
import ru.bgcrm.plugin.bgbilling.ContractTypesConfig;
import ru.bgcrm.plugin.bgbilling.model.ContractType;
import ru.bgcrm.plugin.bgbilling.proto.model.Contract;
import ru.bgcrm.struts.form.DynActionForm;

@Bean
public class CreateContractsStep extends BaseStep {
    private final ContractTypesConfig typesConfig;
    private final boolean showContractTitle;

    public CreateContractsStep(ConfigMap config) {
        super(config);
        typesConfig = new ContractTypesConfig(config, "contractType.");
        showContractTitle = config.getBoolean("showContractTitle", false);
    }

    public Map<Integer, ContractType> getTypeMap() {
        return typesConfig.getTypeMap();
    }

    public boolean getShowContractTitle() {
        return showContractTitle;
    }

    @Override
    public String getJsp() {
        return PATH_JSP + "/step_create_contracts.jsp";
    }

    @Override
    public StepData<?> data(WizardData data) {
        return new Data(this, data);
    }

    public static class Data extends StepData<CreateContractsStep> {
        private Customer customer;
        private List<CommonObjectLink> contractLinkList;

        private Data(CreateContractsStep step, WizardData data) {
            super(step, data);
        }

        public List<CommonObjectLink> getContractLinkList() {
            return contractLinkList;
        }

        public Customer getCustomer() {
            return customer;
        }

        public List<ContractType> getAllowedTypeList() {
            ArrayList<ContractType> result = new ArrayList<>();

            Set<Integer> existsTypes = new HashSet<>();
            for (CommonObjectLink link : contractLinkList) {
                existsTypes.add(link.getConfigMap().getInt("type", 0));
            }

            for (ContractType type : step.getTypeMap().values()) {
                if (!existsTypes.contains(type.getId())) {
                    result.add(type);
                }
            }

            return result;
        }

        @Override
        public boolean isFilled(DynActionForm form, Connection con) {
            List<StepData<?>> stepDataList = data.getStepDataList();

            // находим первый предшествующий шаг заполнения параметра с адресом и выбором контрагента
            // TODO: может в последствии сделать первый предшествующий с нужным параметром
            for (int i = stepDataList.indexOf(this); i >= 0; i--) {
                StepData<?> stepData = stepDataList.get(i);

                if (customer == null && stepData instanceof LinkCustomerStep.Data scStepData) {
                    customer = scStepData.getCustomer();
                }

                if (customer != null) {
                    break;
                }
            }

            contractLinkList = new ProcessLinkDAO(con).getObjectLinksWithType(data.getProcess().getId(), Contract.OBJECT_TYPE + "%");

            return contractLinkList.size() > 0;
        }
    }
}