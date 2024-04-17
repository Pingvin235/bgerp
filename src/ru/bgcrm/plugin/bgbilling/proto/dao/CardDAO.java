package ru.bgcrm.plugin.bgbilling.proto.dao;

import java.util.ArrayList;
import java.util.List;

import org.bgerp.model.base.IdTitle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.bgbilling.DBInfo;
import ru.bgcrm.plugin.bgbilling.Request;
import ru.bgcrm.plugin.bgbilling.proto.model.card.CardActivationData;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.XMLUtils;

public class CardDAO extends BillingModuleDAO {
    private static final String CARD_MODULE_ID = "card";

    public CardDAO(User user, String billingId, int moduleId) {
        super(user, billingId, moduleId);
    }

    public CardDAO(User user, DBInfo dbInfo, int moduleId) {
        super(user, dbInfo.getId(), moduleId);
    }

    public List<IdTitle> CardContractInfo(int contractId, int serviceId, List<CardActivationData> activeCardList) {

        Request request = new Request();
        request.setModule(CARD_MODULE_ID);
        request.setAction("CardContractInfo");
        request.setModuleID(String.valueOf(moduleId));
        request.setContractId(contractId);
        request.setAttribute("list", "1");
        request.setAttribute("sid", serviceId);
        Document doc = transferData.postData(request, user);
        List<IdTitle> serviceList = new ArrayList<>();
        for (Element el : XMLUtils.selectElements(doc, "/data/services/item")) {
            IdTitle item = new IdTitle();
            item.setId(Utils.parseInt(el.getAttribute("id")));
            item.setTitle(el.getAttribute("title"));
            serviceList.add(item);
        }
        activeCardList.clear();
        for (Element el : XMLUtils.selectElements(doc, "/data/table/data/row")) {
            CardActivationData item = new CardActivationData();
            item.setStatus(el.getAttribute("f0"));
            item.setActivationDate(el.getAttribute("f1"));
            item.setSumma(el.getAttribute("f2"));
            item.setNumber(el.getAttribute("f3"));
            activeCardList.add(item);
        }

        return serviceList;
    }

    public void activateCard(int contractId, String cardCode, String pinCode) {
        Request request = new Request();
        request.setModule(CARD_MODULE_ID);
        request.setModuleID(String.valueOf(moduleId));
        request.setAction("ActiveCard");
        request.setContractId(contractId);
        request.setAttribute("cardCode", cardCode.trim());
        request.setAttribute("cardPinCode", pinCode.trim());
        transferData.postData(request, user);

    }

}