package ru.bgcrm.plugin.bgbilling.dao;

import java.sql.Connection;

import org.bgerp.app.exception.BGException;
import org.bgerp.model.Pageable;

import ru.bgcrm.dao.CommonDAO;
import ru.bgcrm.dao.CustomerLinkDAO;
import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.customer.Customer;
import ru.bgcrm.plugin.bgbilling.proto.model.Contract;
import ru.bgcrm.util.Utils;

public class ContractCustomerDAO extends CommonDAO {
    public ContractCustomerDAO(Connection con) {
        super(con);
    }

    public Customer getContractCustomer(Contract contract) throws BGException {
        CommonObjectLink link = new CommonObjectLink();
        link.setLinkObjectType(Contract.OBJECT_TYPE + ":" + contract.getBillingId());
        link.setLinkObjectId(contract.getId());

        Pageable<Customer> customerSearch = new Pageable<Customer>();
        new CustomerLinkDAO(con).searchCustomerByLink(customerSearch, link);

        return Utils.getFirst(customerSearch.getList());
    }
}
