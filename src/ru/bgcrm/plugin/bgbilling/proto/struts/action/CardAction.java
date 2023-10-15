package ru.bgcrm.plugin.bgbilling.proto.struts.action;

import java.util.ArrayList;
import java.util.List;

import org.apache.struts.action.ActionForward;
import org.bgerp.model.base.IdTitle;

import ru.bgcrm.model.BGException;
import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.plugin.bgbilling.Plugin;
import ru.bgcrm.plugin.bgbilling.proto.dao.CardDAO;
import ru.bgcrm.plugin.bgbilling.proto.model.card.CardActivationData;
import ru.bgcrm.plugin.bgbilling.struts.action.BaseAction;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path = "/user/plugin/bgbilling/proto/card")
public class CardAction extends BaseAction {
    private static final String PATH_JSP = Plugin.PATH_JSP_USER + "/card";

    public ActionForward contractInfo(DynActionForm form, ConnectionSet conSet) throws BGException {
        String billingId = form.getParam("billingId");
        int contractId = form.getParamInt("contractId");
        int moduleId = form.getParamInt("moduleId");
        int serviceId = form.getParamInt("serviceId", -1);

        List<CardActivationData> cardList = new ArrayList<>();
        if (serviceId == -1) {
            // попробуем взять первую услугу
            List<IdTitle> sList = new CardDAO(form.getUser(), billingId, moduleId).CardContractInfo(contractId, serviceId, cardList);
            if (sList.size() > 0) {
                serviceId = sList.get(0).getId();
            }
        }

        cardList = new ArrayList<>();
        form.getResponse().setData("serviceList", new CardDAO(form.getUser(), billingId, moduleId).CardContractInfo(contractId, serviceId, cardList));
        form.getResponse().setData("cardList", cardList);

        return html(conSet, form, PATH_JSP + "/contract.jsp");
    }

    public ActionForward activateCard(DynActionForm form, ConnectionSet conSet) throws BGMessageException {
        String billingId = form.getParam("billingId");
        int contractId = form.getParamInt("contractId");
        int moduleId = form.getParamInt("moduleId");

        String cardCode = form.getParam("cardCode", "");
        String pinCode = form.getParam("pinCode", "");

        if (Utils.isEmptyString(cardCode))
            throw new BGMessageException("Введите номер карты");
        if (Utils.isEmptyString(pinCode))
            throw new BGMessageException("Введите PIN-код карты");

        new CardDAO(form.getUser(), billingId, moduleId).activateCard(contractId, cardCode, pinCode);
        form.setParam("cardCode", "");
        form.setParam("pinCode", "");

        return contractInfo(form, conSet);
    }
}