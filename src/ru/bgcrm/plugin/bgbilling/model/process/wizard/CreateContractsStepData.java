package ru.bgcrm.plugin.bgbilling.model.process.wizard;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ru.bgcrm.dao.process.ProcessLinkDAO;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.customer.Customer;
import ru.bgcrm.model.process.wizard.LinkCustomerStepData;
import ru.bgcrm.model.process.wizard.StepData;
import ru.bgcrm.model.process.wizard.WizardData;
import ru.bgcrm.plugin.bgbilling.model.ContractType;
import ru.bgcrm.plugin.bgbilling.proto.model.Contract;
import ru.bgcrm.struts.form.DynActionForm;

public class CreateContractsStepData extends StepData<CreateContractsStep> {
    private Customer customer;
    private List<CommonObjectLink> contractLinkList;

    public CreateContractsStepData(CreateContractsStep step, WizardData data) {
        super(step, data);
    }

    @Override
    public boolean isFilled(DynActionForm form, Connection con) throws BGException {
        List<StepData<?>> stepDataList = data.getStepDataList();

        ProcessLinkDAO linkDao = new ProcessLinkDAO(con);

        contractLinkList = linkDao.getObjectLinksWithType(data.getProcess().getId(), Contract.OBJECT_TYPE + "%");

        // находим первый предшествующий шаг заполнения параметра с адресом и выбором
        // контрагента
        // TODO: может в последствии сделать первый предшествующий с нужным параметром
        for (int i = stepDataList.indexOf(this); i >= 0; i--) {
            StepData<?> stepData = stepDataList.get(i);

            if (customer == null && stepData instanceof LinkCustomerStepData) {
                customer = ((LinkCustomerStepData) stepData).getCustomer();
            }

            if (customer != null) {
                break;
            }
        }

        return contractLinkList.size() > 0;
    }

    public List<ContractType> getAllowedTypeList() {
        ArrayList<ContractType> result = new ArrayList<ContractType>();

        Set<Integer> existsTypes = new HashSet<Integer>();
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

    public List<CommonObjectLink> getContractLinkList() {
        return contractLinkList;
    }

    public Customer getCustomer() {
        return customer;
    }

    public boolean getShowContractTitle() {
        return step.getShowContractTitle();
    }
}