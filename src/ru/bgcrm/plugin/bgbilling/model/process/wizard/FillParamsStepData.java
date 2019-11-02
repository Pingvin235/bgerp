package ru.bgcrm.plugin.bgbilling.model.process.wizard;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import ru.bgcrm.dao.process.ProcessLinkDAO;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.process.wizard.StepData;
import ru.bgcrm.model.process.wizard.WizardData;
import ru.bgcrm.plugin.bgbilling.proto.dao.ContractParamDAO;
import ru.bgcrm.plugin.bgbilling.proto.model.Contract;
import ru.bgcrm.plugin.bgbilling.proto.model.ContractParameter;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;

public class FillParamsStepData extends StepData<FillParamsStep> {
	private Contract contract;
	private List<ContractParameter> values;

	public FillParamsStepData(FillParamsStep step, WizardData data) {
		super(step, data);
	}

	@Override
	public boolean isFilled(DynActionForm form, Connection con) throws BGException {
		CommonObjectLink contractLink = Utils.getFirst(new ProcessLinkDAO(con).getObjectLinksWithType(data.getProcess().getId(), Contract.OBJECT_TYPE + "%"));
		if (contractLink == null)
			return false;
		
		contract = new Contract(StringUtils.substringAfter(contractLink.getLinkedObjectType(), ":"), 
				contractLink.getLinkedObjectId());
		
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

	public Contract getContract() {
		return contract;
	}
	
	public List<ContractParameter> getValues() {
		return values;
	}
}
