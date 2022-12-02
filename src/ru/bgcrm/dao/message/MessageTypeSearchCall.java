package ru.bgcrm.dao.message;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.bgerp.model.Pageable;
import org.bgerp.util.Log;

import ru.bgcrm.dao.CustomerDAO;
import ru.bgcrm.dao.expression.Expression;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.customer.Customer;
import ru.bgcrm.model.message.Message;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

public class MessageTypeSearchCall extends MessageTypeSearch {
    private static final Log log = Log.getLog();

    private String phonePreprocessJexl;
    private List<String> commands;

    public MessageTypeSearchCall(ParameterMap config) throws BGException {
        super(config);

        // может добавится customerByTextParam:<paramId>
        this.commands = Utils.toList(config.get("commands"));
        this.phonePreprocessJexl = config.get(Expression.STRING_MAKE_EXPRESSION_CONFIG_KEY + "NumberPreprocess");
    }

    @Override
    public void search(DynActionForm form, ConnectionSet conSet, Message message, Set<CommonObjectLink> result) throws BGException {
        String numberFrom = preprocessNumber(message, phonePreprocessJexl);

        log.debug("Search by numberFrom: {}", numberFrom);

        for (String command : commands) {
            if (command.startsWith("customerByPhoneParam:")) {
                String paramIds = StringUtils.substringAfter(command, ":");

                Pageable<Customer> searchResult = new Pageable<Customer>();

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

            Map<String, Object> map = new HashMap<String, Object>(1);
            map.put("numberFrom", message.getFrom());

            numberFrom = new Expression(map).getString(phonePreprocessJexl);

            log.debug("Number preprocessed: {} => {}", message.getFrom(), numberFrom);
        }

        return numberFrom;
    }
}