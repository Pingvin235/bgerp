package ru.bgcrm.plugin.bgbilling.dao;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.bgerp.model.Pageable;
import org.bgerp.util.Log;

import ru.bgcrm.dao.expression.Expression;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.message.Message;
import ru.bgcrm.plugin.bgbilling.DBInfo;
import ru.bgcrm.plugin.bgbilling.DBInfoManager;
import ru.bgcrm.plugin.bgbilling.proto.dao.ContractDAO;
import ru.bgcrm.plugin.bgbilling.proto.model.Contract;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

public class MessageTypeSearchCall extends MessageTypeSearchBilling {
    private static final Log log = Log.getLog();

    private final List<String> commands;
    private final String phonePreprocessJexl;

    public MessageTypeSearchCall(ParameterMap config) throws BGException {
        super(config);

        // contractByTextParam:<paramId>;contractByComment
        this.commands = Utils.toList(config.get("commands"));
        this.phonePreprocessJexl = config.get(Expression.STRING_MAKE_EXPRESSION_CONFIG_KEY + "NumberPreprocess");
    }

    @Override
    public void search(DynActionForm form, ConnectionSet conSet, Message message, Set<CommonObjectLink> result)
            throws BGException {
        String numberFrom = ru.bgcrm.dao.message.MessageTypeSearchCall.preprocessNumber(message, phonePreprocessJexl);

        if (log.isDebugEnabled()) {
            log.debug("Search by numberFrom: " + numberFrom);
        }

        DBInfo dbInfo = DBInfoManager.getDbInfo(billingId);
        if (dbInfo == null) {
            log.warn("Billing not found: " + billingId);
            return;
        }

        for (String command : commands) {
            if (command.startsWith("contractByComment:")) {
                //new ContractDAO( form.getUser(), billingId ).searchContractByTitleComment( searchResult, null, comment, searchOptions )
            } else if (command.startsWith("contractByPhoneParam:")) {
                String paramIds = StringUtils.substringAfter(command, ":");

                Pageable<Contract> searchResult = new Pageable<Contract>();
                ContractDAO.getInstance(form.getUser(), billingId).searchContractByPhoneParam(searchResult, null,
                        Utils.toIntegerSet(paramIds), numberFrom);

                for (Contract contract : searchResult.getList()) {
                    result.add(new CommonObjectLink(0, Contract.OBJECT_TYPE + ":" + contract.getBillingId(),
                            contract.getId(), contract.getTitle(), contract.getComment()));
                }
            }
        }
    }
}