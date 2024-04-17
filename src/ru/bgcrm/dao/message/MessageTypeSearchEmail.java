package ru.bgcrm.dao.message;

import java.util.Set;

import org.bgerp.app.bean.annotation.Bean;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.cache.ParameterCache;
import org.bgerp.model.Pageable;

import ru.bgcrm.dao.CustomerDAO;
import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.customer.Customer;
import ru.bgcrm.model.message.Message;
import ru.bgcrm.model.param.ParameterSearchedObject;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

// TODO: Сделать параметры с конфигурацией что искать.
@Bean
public class MessageTypeSearchEmail extends MessageTypeSearch {
    public MessageTypeSearchEmail(ConfigMap config) {
        super(config);
    }

    @Override
    public void search(DynActionForm form, ConnectionSet conSet, Message message, Set<CommonObjectLink> result) {
        String email = message.getFrom();

        Pageable<ParameterSearchedObject<Customer>> searchResult = new Pageable<ParameterSearchedObject<Customer>>();
        new CustomerDAO(conSet.getConnection()).searchCustomerListByEmail(searchResult,
                Utils.getObjectIdsList(ParameterCache.getObjectTypeParameterList(Customer.OBJECT_TYPE)), email);

        for (ParameterSearchedObject<Customer> object : searchResult.getList()) {
            result.add(new CommonObjectLink(0, Customer.OBJECT_TYPE, object.getObject().getId(), object.getObject().getTitle()));
        }
    }
}
