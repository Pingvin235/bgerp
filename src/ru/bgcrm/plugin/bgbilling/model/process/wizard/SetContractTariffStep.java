package ru.bgcrm.plugin.bgbilling.model.process.wizard;

import org.bgerp.app.cfg.ConfigMap;

import ru.bgcrm.model.process.wizard.Step;
import ru.bgcrm.model.process.wizard.StepData;
import ru.bgcrm.model.process.wizard.WizardData;

public class SetContractTariffStep extends Step {
	public SetContractTariffStep(ConfigMap config) {
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
