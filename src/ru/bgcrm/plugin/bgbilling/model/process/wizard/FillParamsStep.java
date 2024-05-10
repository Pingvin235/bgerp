package ru.bgcrm.plugin.bgbilling.model.process.wizard;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bgerp.app.bean.annotation.Bean;
import org.bgerp.app.cfg.ConfigMap;

import ru.bgcrm.dao.process.ProcessLinkDAO;
import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.process.wizard.base.StepData;
import ru.bgcrm.model.process.wizard.base.WizardData;
import ru.bgcrm.plugin.bgbilling.proto.dao.ContractParamDAO;
import ru.bgcrm.plugin.bgbilling.proto.model.Contract;
import ru.bgcrm.plugin.bgbilling.proto.model.ContractParameter;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;

@Bean
public class FillParamsStep extends BaseStep {
    private final List<Integer> parameterIds;
    private final List<Integer> checkParamIds;

    public FillParamsStep(ConfigMap config) {
        super(config);
        parameterIds = Utils.toIntegerList(config.get("parameterIds", ""));
        checkParamIds = Utils.toIntegerList(config.get("checkParamIds", ""));
    }

    public List<Integer> getParameterIds() {
        return parameterIds;
    }

    public List<Integer> getCheckParamIds() {
        return checkParamIds;
    }

    @Override
    public String getJsp() {
        return PATH_JSP + "/step_fill_params.jsp";
    }

    @Override
    public StepData<?> data(WizardData data) {
        return new Data(this, data);
    }

    public static class Data extends StepData<FillParamsStep> {
        private Contract contract;
        private List<ContractParameter> values;

        private Data(FillParamsStep step, WizardData data) {
            super(step, data);
        }

        public Contract getContract() {
            return contract;
        }

        public List<ContractParameter> getValues() {
            return values;
        }

        @Override
        public boolean isFilled(DynActionForm form, Connection con) {
            CommonObjectLink contractLink = Utils.getFirst(new ProcessLinkDAO(con).getObjectLinksWithType(data.getProcess().getId(), Contract.OBJECT_TYPE + "%"));
            if (contractLink == null)
                return false;

            contract = new Contract(contractLink);

            Set<Integer> checkParamIds = new HashSet<>(step.getCheckParamIds());

            ContractParamDAO paramDao = new ContractParamDAO(form.getUser(), contract.getBillingId());
            List<ContractParameter> allParamValues = paramDao.getParameterList(contract.getId());

            List<ContractParameter> filteredValues = new ArrayList<>(step.getParameterIds().size());
            for (int paramId : step.getParameterIds()) {
                ContractParameter param = allParamValues.stream().filter(cp -> cp.getParamId() == paramId).findFirst().orElse(null);
                if (param != null) {
                    filteredValues.add(param);
                    if (Utils.notBlankString(param.getValue()))
                        checkParamIds.remove(paramId);
                }
                values = filteredValues;
            }

            return checkParamIds.isEmpty();
        }
    }
}
