package ru.bgcrm.plugin.bgbilling.model.process.wizard;

import ru.bgcrm.model.process.wizard.Step;
import ru.bgcrm.model.process.wizard.StepData;
import ru.bgcrm.model.process.wizard.WizardData;
import ru.bgcrm.util.ParameterMap;

public class SetContractTariffStep extends Step {
	public SetContractTariffStep(ParameterMap config) {
		super(config);
	}

	@Override
	public String getJspFile() {
		return "/WEB-INF/jspf/usermob/plugin/bgbilling/step_set_contract_tariff.jsp";
	}

	@Override
	public StepData<?> newStepData(WizardData data) {
		return new SetContractTariffStepData(this, data);
	}
}
