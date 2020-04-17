package ru.bgcrm.dao.message;

import java.util.Set;

import ru.bgcrm.dao.CustomerDAO;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.Customer;
import ru.bgcrm.model.SearchResult;
import ru.bgcrm.model.message.Message;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.sql.ConnectionSet;

public class MessageTypeSearchCustomerByTitle extends MessageTypeSearch {
    public MessageTypeSearchCustomerByTitle(ParameterMap config) throws BGException {
        super(config);
    }

    @Override
    public String getJsp() {
        return "/WEB-INF/jspf/user/message/message_search_customer_title.jsp";
    }

    @Override
    public void search(DynActionForm form, ConnectionSet conSet, Message message, Set<CommonObjectLink> result) throws BGException {
        SearchResult<Customer> searchResult = new SearchResult<Customer>();
        new CustomerDAO(conSet.getConnection()).searchCustomerList(searchResult, "%" + form.getParam("title") + "%");

        for (Customer customer : searchResult.getList()) {
            result.add(new CommonObjectLink(0, Customer.OBJECT_TYPE, customer.getId(), customer.getTitle()));
        }
    }
}