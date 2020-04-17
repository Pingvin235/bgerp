package ru.bgcrm.model.process.wizard;

import java.sql.Connection;
import java.util.List;

import ru.bgcrm.dao.CustomerDAO;
import ru.bgcrm.dao.process.ProcessLinkDAO;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.Customer;
import ru.bgcrm.struts.form.DynActionForm;

public class LinkCustomerStepData extends StepData<LinkCustomerStep> {
    private Customer customer;

    public LinkCustomerStepData(LinkCustomerStep step, WizardData data) {
        super(step, data);
    }

    @Override
    public boolean isFilled(DynActionForm form, Connection con) throws BGException {
        //TODO: Предусмотреть одну выборку с джойном.
        List<CommonObjectLink> linkList = new ProcessLinkDAO(con).getObjectLinksWithType(data.getProcess().getId(),
                Customer.OBJECT_TYPE);
        if (linkList.size() > 0) {
            customer = new CustomerDAO(con).getCustomerById(linkList.get(0).getLinkedObjectId());
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
