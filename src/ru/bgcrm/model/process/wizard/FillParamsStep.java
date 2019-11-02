package ru.bgcrm.model.process.wizard;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import ru.bgcrm.cache.ParameterCache;
import ru.bgcrm.model.param.Parameter;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Utils;

public class FillParamsStep extends Step {
	private static final Logger log = Logger.getLogger(FillParamsStep.class);
	
	private final String type;
	private final Set<Integer> checkParamIds;

	public FillParamsStep(ParameterMap config) {
		super(config);
		type = config.get("object");
		checkParamIds = Utils.toIntegerSet(config.get("checkParamIds", ""));
	}

	@Override
	public String getJspFile() {
		return "/WEB-INF/jspf/usermob/process/process/wizard/step_fill_params.jsp";
	}

	@Override
	public FillParamsStepData newStepData(WizardData data) {
		return new FillParamsStepData(this, data);
	}
	
	public Set<Integer> getCheckParamIds() {
		return checkParamIds;
	}

	public String getType() {
		return type;
	}

	public List<Parameter> getParamList() {
		List<Parameter> paramList = new ArrayList<Parameter>();

		// сохранение списка параметров убрано, чтобы из кэша брались актуальные параметры
		for (int paramId : Utils.toIntegerList(config.get("parameterIds"))) {
			Parameter param = ParameterCache.getParameter(paramId);
			if (param == null) {
				log.error("Not found param: " + paramId);
				continue;
			}
			paramList.add(param);
		}

		return paramList;
	}
}
