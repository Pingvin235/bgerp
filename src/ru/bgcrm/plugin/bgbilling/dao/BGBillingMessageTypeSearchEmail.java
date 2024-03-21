package ru.bgcrm.plugin.bgbilling.dao;

import java.util.Set;

import org.bgerp.app.bean.annotation.Bean;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.exception.BGException;
import org.bgerp.model.Pageable;

import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.message.Message;
import ru.bgcrm.plugin.bgbilling.proto.dao.ContractDAO;
import ru.bgcrm.plugin.bgbilling.proto.model.Contract;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

@Bean(oldClasses = "ru.bgcrm.plugin.bgbilling.dao.MessageTypeSearchEmail")
public class BGBillingMessageTypeSearchEmail extends MessageTypeSearchBilling {
    private final Set<Integer> paramIds;

    public BGBillingMessageTypeSearchEmail(ConfigMap config) throws BGException {
        super(config);
        paramIds = Utils.toIntegerSet(config.get("paramIds"));
    }

    @Override
    public void search(DynActionForm form, ConnectionSet conSet, Message message, Set<CommonObjectLink> result)
            throws BGException {
        String email = message.getFrom();

        Pageable<Contract> searchResult = new Pageable<Contract>();
        ContractDAO.getInstance(form.getUser(), billingId).searchContractByEmailParam(searchResult, null, paramIds,
                email);

        for (Contract contract : searchResult.getList()) {
            result.add(new CommonObjectLink(0, Contract.OBJECT_TYPE + ":" + contract.getBillingId(), contract.getId(),
                    contract.getTitle(), contract.getComment()));
        }
    }
}