package org.bgerp.plugin.msg.email.message;

import java.util.Set;

import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.cfg.bean.annotation.Bean;
import org.bgerp.cache.ParameterCache;
import org.bgerp.dao.customer.CustomerDAO;
import org.bgerp.model.Pageable;
import org.bgerp.model.msg.Message;

import ru.bgcrm.dao.message.MessageTypeSearch;
import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.customer.Customer;
import ru.bgcrm.model.param.ParameterSearchedObject;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

// TODO: Сделать параметры с конфигурацией что искать.
@Bean(oldClasses = "ru.bgcrm.dao.message.MessageTypeSearchEmail")
public class MessageTypeSearchEmail extends MessageTypeSearch {
    public MessageTypeSearchEmail(ConfigMap config) {
        super(config);
    }

    @Override
    public void search(DynActionForm form, ConnectionSet conSet, Message message, Set<CommonObjectLink> result) {
        String email = message.getFrom();

        Pageable<ParameterSearchedObject<Customer>> searchResult = new Pageable<>();
        new CustomerDAO(conSet.getConnection()).searchCustomerListByEmail(searchResult,
                Utils.getObjectIdsList(ParameterCache.getObjectTypeParameterList(Customer.OBJECT_TYPE)), email);

        for (ParameterSearchedObject<Customer> object : searchResult.getList()) {
            result.add(new CommonObjectLink(0, Customer.OBJECT_TYPE, object.getObject().getId(), object.getObject().getTitle()));
        }
    }
}
