package ru.bgcrm.plugin.bgbilling.model.process.wizard;

import java.util.List;

import ru.bgcrm.model.process.wizard.Step;
import ru.bgcrm.model.process.wizard.StepData;
import ru.bgcrm.model.process.wizard.WizardData;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Utils;

public class FillParamsStep extends Step {
	private final List<Integer> parameterIds;
	private final List<Integer> checkParamIds;

	public FillParamsStep(ParameterMap config) {
		super(config);
		parameterIds = Utils.toIntegerList(config.get("parameterIds", ""));
		checkParamIds = Utils.toIntegerList(config.get("checkParamIds", ""));
	}

	@Override
	public String getJspFile() {
		return "/WEB-INF/jspf/usermob/plugin/bgbilling/step_fill_params.jsp";
	}

	@Override
	public StepData<?> newStepData(WizardData data) {
		return new FillParamsStepData(this, data);
	}

	public List<Integer> getParameterIds() {
		return parameterIds;
	}

	public List<Integer> getCheckParamIds() {
		return checkParamIds;
	}
}
