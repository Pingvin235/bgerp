package ru.bgcrm.dao.message;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.cfg.bean.annotation.Bean;
import org.bgerp.dao.customer.CustomerDAO;
import org.bgerp.dao.expression.Expression;
import org.bgerp.model.Pageable;
import org.bgerp.model.msg.Message;
import org.bgerp.util.Log;

import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.customer.Customer;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

@Bean
public class MessageTypeSearchCall extends MessageTypeSearch {
    private static final Log log = Log.getLog();

    private String phonePreprocessJexl;
    private List<String> commands;

    public MessageTypeSearchCall(ConfigMap config) {
        super(config);

        this.commands = Utils.toList(config.get("commands"));
        this.phonePreprocessJexl = config.getSok(Expression.EXPRESSION_CONFIG_KEY + "NumberPreprocess", "stringExpressionNumberPreprocess");
    }

    @Override
    public void search(DynActionForm form, ConnectionSet conSet, Message message, Set<CommonObjectLink> result) {
        String numberFrom = preprocessNumber(message, phonePreprocessJexl);

        log.debug("Search by numberFrom: {}", numberFrom);

        for (String command : commands) {
            if (command.startsWith("customerByPhoneParam:")) {
                String paramIds = StringUtils.substringAfter(command, ":");

                Pageable<Customer> searchResult = new Pageable<>();

                new CustomerDAO(conSet.getConnection()).searchCustomerListByPhone(searchResult,
                        Utils.toIntegerSet(paramIds), numberFrom);

                for (Customer customer : searchResult.getList()) {
                    result.add(new CommonObjectLink(0, Customer.OBJECT_TYPE, customer.getId(), customer.getTitle()));
                }
            }
        }
    }

    public static String preprocessNumber(Message message, String phonePreprocessJexl) {
        String numberFrom = message.getFrom();

        if (Utils.notBlankString(phonePreprocessJexl)) {
            log.debug("Using preprocess JEXL: '{}'", phonePreprocessJexl);

            Map<String, Object> map = new HashMap<>(1);
            map.put("numberFrom", message.getFrom());

            numberFrom = new Expression(map).executeGetString(phonePreprocessJexl);

            log.debug("Number preprocessed: {} => {}", message.getFrom(), numberFrom);
        }

        return numberFrom;
    }
}