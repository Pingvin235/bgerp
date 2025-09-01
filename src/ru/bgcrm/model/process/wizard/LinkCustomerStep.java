package ru.bgcrm.model.process.wizard;

import java.sql.Connection;
import java.util.List;

import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.cfg.bean.annotation.Bean;
import org.bgerp.dao.customer.CustomerDAO;

import ru.bgcrm.dao.process.ProcessLinkDAO;
import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.customer.Customer;
import ru.bgcrm.model.process.wizard.base.Step;
import ru.bgcrm.model.process.wizard.base.StepData;
import ru.bgcrm.model.process.wizard.base.WizardData;
import ru.bgcrm.struts.form.DynActionForm;

@Bean
public class LinkCustomerStep extends Step {
    private final int paramGroupId;

    public LinkCustomerStep(ConfigMap config) {
        super(config);
        paramGroupId = config.getInt("paramGroupId", 0);
    }

    public int getParamGroupId() {
        return paramGroupId;
    }

    @Override
    public String getJsp() {
        return PATH_JSP + "/step_link_customer.jsp";
    }

    @Override
    public Data data(WizardData data) {
        return new Data(this, data);
    }

    public static class Data extends StepData<LinkCustomerStep> {
        private Customer customer;

        private Data(LinkCustomerStep step, WizardData data) {
            super(step, data);
        }

        @Override
        public boolean isFilled(DynActionForm form, Connection con) {
            //TODO: Предусмотреть одну выборку с джойном.
            List<CommonObjectLink> linkList = new ProcessLinkDAO(con).getObjectLinksWithType(data.getProcess().getId(), Customer.OBJECT_TYPE);
            if (linkList.size() > 0) {
                customer = new CustomerDAO(con).getCustomerById(linkList.get(0).getLinkObjectId());
            }

            return customer != null;
        }

        public Customer getCustomer() {
            return customer;
        }

        public int getParamGroupId() {
            return step.getParamGroupId();
        }
    }
}
