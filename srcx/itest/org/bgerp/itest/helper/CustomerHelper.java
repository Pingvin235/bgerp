package org.bgerp.itest.helper;

import org.bgerp.itest.kernel.db.DbTest;

import ru.bgcrm.dao.CustomerDAO;
import ru.bgcrm.model.customer.Customer;

public class CustomerHelper {

    public static Customer addCustomer(int titlePatternId, int paramGroupId, String title) throws Exception {
        var con = DbTest.conRoot;
        var dao = new CustomerDAO(con);
        
        Customer customer = new Customer();
        customer.setTitlePatternId(titlePatternId);
        customer.setParamGroupId(paramGroupId);
        customer.setTitle(title);

        dao.updateCustomer(customer);

        return customer;
    }

}