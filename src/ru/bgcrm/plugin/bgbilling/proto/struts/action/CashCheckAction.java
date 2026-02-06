package ru.bgcrm.plugin.bgbilling.proto.struts.action;

import org.apache.struts.action.ActionForward;
import org.bgerp.app.exception.BGMessageException;

import ru.bgcrm.model.Pair;
import ru.bgcrm.plugin.bgbilling.action.proto.ContractAction;
import ru.bgcrm.plugin.bgbilling.proto.dao.CashCheckDAO;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path = "/user/plugin/bgbilling/proto/cashcheck")
public class CashCheckAction extends ContractAction {
    public ActionForward registratorList(DynActionForm form, ConnectionSet conSet) {
        String billingId = form.getParam("billingId");
        CashCheckDAO cashCkeckDao = new CashCheckDAO(form.getUser(), billingId);

        form.setResponseData("registratorList", cashCkeckDao.getRegistratorList());

        return json(conSet, form);
    }

    public ActionForward printCheck(DynActionForm form, ConnectionSet conSet) throws BGMessageException {
        String billingId = form.getParam("billingId");
        int paymentId = form.getParamInt("paymentId");

        CashCheckDAO cashCkeckDao = new CashCheckDAO(form.getUser(), billingId);
        Pair<String, String> result = cashCkeckDao.printCheck(form.getParamInt("selectedRegisterId"), paymentId,
                form.getParam("clientCash"), form.getParam("selectedRegisterPswd"));

        form.setResponseData("summa", result.getFirst());
        form.setResponseData("submit", result.getSecond());

        return json(conSet, form);
    }
}