package ru.bgcrm.event.listener;

import org.bgerp.app.event.EventProcessor;
import org.bgerp.app.exception.BGException;
import org.bgerp.model.param.Parameter;
import org.bgerp.util.Log;

import ru.bgcrm.dao.CustomerDAO;
import ru.bgcrm.event.ParamChangedEvent;
import ru.bgcrm.model.customer.Customer;
import ru.bgcrm.util.sql.ConnectionSet;

public class CustomerTitleListener {
    private static final Log log = Log.getLog();

    public CustomerTitleListener() {
        EventProcessor.subscribe((e, conSet) -> paramChanged(e, conSet), ParamChangedEvent.class);
    }

    private void paramChanged(ParamChangedEvent e, ConnectionSet conSet) {
        Parameter param = e.getParameter();
        if (Customer.OBJECT_TYPE.equals(param.getObjectType())) {
            try {
                CustomerDAO customerDAO = new CustomerDAO(conSet.getConnection());

                Customer customer = customerDAO.getCustomerById(e.getObjectId());
                if (customer == null) {
                    throw new BGException("Customer not found with id: " + e.getObjectId());
                }

                customerDAO.updateCustomerTitle(customer.getTitle(), customer, param.getId(), e.getForm().getResponse());
            } catch (Exception ex) {
                log.error(ex);
            }
        }
    }
}
