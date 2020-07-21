package ru.bgcrm.plugin.bgbilling.dao;

import java.sql.Connection;

import ru.bgcrm.dao.CommonDAO;
import ru.bgcrm.dao.CustomerLinkDAO;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.SearchResult;
import ru.bgcrm.model.customer.Customer;
import ru.bgcrm.plugin.bgbilling.proto.model.Contract;
import ru.bgcrm.util.Utils;

public class ContractCustomerDAO
	extends CommonDAO
{
	public ContractCustomerDAO( Connection con )
	{
		super( con );
	}
	
	public Customer getContractCustomer( Contract contract )
    	throws BGException
    {
    	// контрагент
    	CommonObjectLink link = new CommonObjectLink();
    	link.setLinkedObjectType( Contract.OBJECT_TYPE + ":" + contract.getBillingId() );
    	link.setLinkedObjectId( contract.getId() );
    
    	SearchResult<Customer> customerSearch = new SearchResult<Customer>();
    	new CustomerLinkDAO( con ).searchCustomerByLink( customerSearch, link );
    
    	return Utils.getFirst( customerSearch.getList() );
    }
}
