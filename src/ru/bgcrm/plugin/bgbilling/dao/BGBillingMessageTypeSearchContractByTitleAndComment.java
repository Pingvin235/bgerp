package ru.bgcrm.plugin.bgbilling.dao;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.bgerp.app.bean.annotation.Bean;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.model.Pageable;
import org.bgerp.model.base.IdTitle;
import org.bgerp.util.Log;

import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.message.Message;
import ru.bgcrm.plugin.bgbilling.DBInfo;
import ru.bgcrm.plugin.bgbilling.DBInfoManager;
import ru.bgcrm.plugin.bgbilling.proto.dao.ContractDAO;
import ru.bgcrm.plugin.bgbilling.proto.dao.ContractDAO.SearchOptions;
import ru.bgcrm.plugin.bgbilling.proto.model.Contract;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

@Bean(oldClasses = "ru.bgcrm.plugin.bgbilling.dao.MessageTypeSearchContractByTitleAndComment")
public class BGBillingMessageTypeSearchContractByTitleAndComment extends MessageTypeSearchBilling {
    private static final Log log = Log.getLog();

    public BGBillingMessageTypeSearchContractByTitleAndComment(ConfigMap config) {
        super(config);
    }

    @Override
    public String getJsp() {
        return "/WEB-INF/jspf/user/plugin/bgbilling/message_search_contract_title_comment.jsp";
    }

    @Override
    public void search(DynActionForm form, ConnectionSet conSet, Message message, Set<CommonObjectLink> result)
            {
        DBInfo dbInfo = DBInfoManager.getDbInfo(billingId);
        if (dbInfo == null) {
            log.warn("Billing not found: " + billingId);
            return;
        }

        String title = form.getParam("title");
        String comment = form.getParam("comment");

        if ((Utils.isBlankString(title) && Utils.isBlankString(comment))
                || (Utils.notBlankString(title) && title.length() < 3)
                || (Utils.notBlankString(comment) && comment.length() < 3)) {
            return;
        }

        Pageable<IdTitle> searchResult = new Pageable<IdTitle>();
        ContractDAO.getInstance(form.getUser(), billingId).searchContractByTitleComment(searchResult, title, comment,
                new SearchOptions(false, false, false));

        for (IdTitle object : searchResult.getList()) {
            result.add(new CommonObjectLink(0, Contract.OBJECT_TYPE + ":" + billingId, object.getId(),
                    StringUtils.substringBeforeLast(object.getTitle(), "[").trim(),
                    StringUtils.substringBetween(object.getTitle(), "[", "]").trim()));
        }
    }
}
