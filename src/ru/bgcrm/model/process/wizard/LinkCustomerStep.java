package ru.bgcrm.model.process.wizard;

import org.bgerp.app.bean.annotation.Bean;
import org.bgerp.app.cfg.ConfigMap;

@Bean
public class LinkCustomerStep extends Step {
    private int paramGroupId;

    public LinkCustomerStep(ConfigMap config) {
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
