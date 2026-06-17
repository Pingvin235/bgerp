package ru.bgcrm.plugin.bgbilling.message;

import java.util.Set;

import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.cfg.bean.annotation.Bean;
import org.bgerp.model.Pageable;
import org.bgerp.model.msg.Message;
import org.bgerp.util.Log;

import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.param.ParameterSearchedObject;
import ru.bgcrm.plugin.bgbilling.DBInfo;
import ru.bgcrm.plugin.bgbilling.DBInfoManager;
import ru.bgcrm.plugin.bgbilling.proto.dao.ContractDAO;
import ru.bgcrm.plugin.bgbilling.proto.model.Contract;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.sql.ConnectionSet;

@Bean
public class BGBillingMessageTypeSearchContractByAddress extends MessageTypeSearchBilling {
    private static final Log log = Log.getLog();

    public BGBillingMessageTypeSearchContractByAddress(ConfigMap config) {
        super(config);
    }

    @Override
    public String getJsp() {
        return "/WEB-INF/jspf/user/plugin/bgbilling/message_search_contract_address.jsp";
    }

    @Override
    public void search(DynActionForm form, ConnectionSet conSet, Message message, Set<CommonObjectLink> result) {
        DBInfo dbInfo = DBInfoManager.getDbInfo(billingId);
        if (dbInfo == null) {
            log.warn("Billing not found: " + billingId);
            return;
        }

        Pageable<ParameterSearchedObject<Contract>> searchResult = new Pageable<>();
        new ContractDAO(form.getUser(), billingId).searchContractByAddressParam(searchResult, null, form.getParamInt("streetId"),
                form.getParamInt("houseId"), form.getParam("house"), form.getParam("flat"), form.getParam("room"));

        for (var pso : searchResult.getList()) {
            var contract = pso.getObject();
            result.add(new CommonObjectLink(0, Contract.OBJECT_TYPE + ":" + contract.getBillingId(), contract.getId(), contract.getTitle(),
                    contract.getComment()));
        }
    }
}
