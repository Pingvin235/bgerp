package ru.bgcrm.plugin.bgbilling.model.process.wizard;

import org.bgerp.app.cfg.ConfigMap;

import ru.bgcrm.model.process.wizard.Step;
import ru.bgcrm.model.process.wizard.StepData;
import ru.bgcrm.model.process.wizard.WizardData;

public class FindContractStep extends Step {
	private final String billingId;

	public FindContractStep(ConfigMap config) {
		super(config);
		billingId = config.get("billingId");
	}

	@Override
	public String getJspFile() {
		return "/WEB-INF/jspf/usermob/plugin/bgbilling/step_find_contract.jsp";
	}

	@Override
	public StepData<?> newStepData(WizardData data) {
		return new FindContractStepData(this, data);
	}

	public String getBillingId() {
		return billingId;
	}
}
