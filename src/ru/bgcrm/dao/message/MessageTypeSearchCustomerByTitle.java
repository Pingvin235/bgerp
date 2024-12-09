package ru.bgcrm.dao.message;

import java.util.Set;

import org.bgerp.app.bean.annotation.Bean;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.model.Pageable;
import org.bgerp.model.msg.Message;

import ru.bgcrm.dao.CustomerDAO;
import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.customer.Customer;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.sql.ConnectionSet;

@Bean
public class MessageTypeSearchCustomerByTitle extends MessageTypeSearch {
    public MessageTypeSearchCustomerByTitle(ConfigMap config) {
        super(config);
    }

    @Override
    public String getJsp() {
        return "/WEB-INF/jspf/user/message/message_search_customer_title.jsp";
    }

    @Override
    public void search(DynActionForm form, ConnectionSet conSet, Message message, Set<CommonObjectLink> result) {
        Pageable<Customer> searchResult = new Pageable<>();
        new CustomerDAO(conSet.getConnection()).searchCustomerList(searchResult, "%" + form.getParam("title") + "%");

        for (Customer customer : searchResult.getList()) {
            result.add(new CommonObjectLink(0, Customer.OBJECT_TYPE, customer.getId(), customer.getTitle()));
        }
    }
}