package ru.bgcrm.plugin.bgbilling.message;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.cfg.bean.annotation.Bean;
import org.bgerp.dao.expression.Expression;
import org.bgerp.model.Pageable;
import org.bgerp.model.msg.Message;
import org.bgerp.util.Log;

import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.plugin.bgbilling.DBInfo;
import ru.bgcrm.plugin.bgbilling.DBInfoManager;
import ru.bgcrm.plugin.bgbilling.proto.dao.ContractDAO;
import ru.bgcrm.plugin.bgbilling.proto.model.Contract;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

@Bean(oldClasses = "ru.bgcrm.plugin.bgbilling.dao.MessageTypeSearchCall")
public class BGBillingMessageTypeSearchCall extends MessageTypeSearchBilling {
    private static final Log log = Log.getLog();

    private final List<String> commands;
    private final String phonePreprocessJexl;

    public BGBillingMessageTypeSearchCall(ConfigMap config) {
        super(config);

        this.commands = Utils.toList(config.get("commands"));
        this.phonePreprocessJexl = config.getSok(Expression.EXPRESSION_CONFIG_KEY + "NumberPreprocess", "stringExpressionNumberPreprocess");
    }

    @Override
    public void search(DynActionForm form, ConnectionSet conSet, Message message, Set<CommonObjectLink> result) {
        String numberFrom = ru.bgcrm.dao.message.MessageTypeSearchCall.preprocessNumber(message, phonePreprocessJexl);

        log.debug("Search by numberFrom: {}", numberFrom);

        DBInfo dbInfo = DBInfoManager.getDbInfo(billingId);
        if (dbInfo == null) {
            log.warn("Billing not found: {}", billingId);
            return;
        }

        for (String command : commands) {
            if (command.startsWith("contractByComment:")) {
                //new ContractDAO( form.getUser(), billingId ).searchContractByTitleComment( searchResult, null, comment, searchOptions )
            } else if (command.startsWith("contractByPhoneParam:")) {
                String paramIds = StringUtils.substringAfter(command, ":");

                Pageable<Contract> searchResult = new Pageable<>();
                new ContractDAO(form.getUser(), billingId).searchContractByPhoneParam(searchResult, null, Utils.toIntegerSet(paramIds), numberFrom);

                for (Contract contract : searchResult.getList()) {
                    result.add(new CommonObjectLink(0, Contract.OBJECT_TYPE + ":" + contract.getBillingId(),
                            contract.getId(), contract.getTitle(), contract.getComment()));
                }
            }
        }
    }
}