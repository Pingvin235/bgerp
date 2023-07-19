package ru.bgcrm.plugin.bgbilling.dao;

import java.util.List;
import java.util.Set;

import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.util.Log;

import ru.bgcrm.model.BGException;
import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.message.Message;
import ru.bgcrm.plugin.bgbilling.DBInfo;
import ru.bgcrm.plugin.bgbilling.DBInfoManager;
import ru.bgcrm.plugin.bgbilling.proto.dao.DialUpDAO;
import ru.bgcrm.plugin.bgbilling.proto.model.Contract;
import ru.bgcrm.plugin.bgbilling.proto.model.dialup.DialUpLogin;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

public class MessageTypeSearchContractByDialUpLogin extends MessageTypeSearchBilling {
    private static final Log log = Log.getLog();

    private int moduleId;

    public MessageTypeSearchContractByDialUpLogin(ConfigMap config) throws BGException {
        super(config);
        this.moduleId = config.getInt("moduleId", 0);
    }

    @Override
    public String getJsp() {
        return "/WEB-INF/jspf/user/plugin/bgbilling/message_search_contract_dialup_login.jsp";
    }

    @Override
    public void search(DynActionForm form, ConnectionSet conSet, Message message, Set<CommonObjectLink> result)
            throws BGException {
        DBInfo dbInfo = DBInfoManager.getDbInfo(billingId);
        if (dbInfo == null) {
            log.warn("Billing not found: " + billingId);
            return;
        }

        String login = form.getParam("login", "");
        if (Utils.isBlankString(login) || login.length() < 3) {
            return;
        }

        DialUpDAO dialUpDao = new DialUpDAO(form.getUser(), billingId, moduleId);
        List<DialUpLogin> searchResult = dialUpDao.findLogin(login,
                Utils.parseInt(login) > 0 ? DialUpDAO.FIND_MODE_LOGIN : DialUpDAO.FIND_MODE_ALIAS);

        for (DialUpLogin object : searchResult) {
            result.add(new CommonObjectLink(0, Contract.OBJECT_TYPE + ":" + billingId, object.getContractId(),
                    object.getContractTitle(), object.getLogin() + " " + object.getAlias()));
        }
    }
}
