package ru.bgcrm.model.process.wizard;

import ru.bgcrm.util.ParameterMap;

public class LinkCustomerStep extends Step {
    private int paramGroupId;

    public LinkCustomerStep(ParameterMap config) {
        super(config);
        paramGroupId = config.getInt("paramGroupId", 0);
    }

    @Override
    public String getJspFile() {
        return "/WEB-INF/jspf/usermob/process/process/wizard/step_link_customer.jsp";
    }

    @Override
    public LinkCustomerStepData newStepData(WizardData data) {
        return new LinkCustomerStepData(this, data);
    }

    public int getParamGroupId() {
        return paramGroupId;
    }
    
}
