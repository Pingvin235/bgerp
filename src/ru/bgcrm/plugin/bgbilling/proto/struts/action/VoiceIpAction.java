package ru.bgcrm.plugin.bgbilling.proto.struts.action;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.struts.action.ActionForward;

import ru.bgcrm.model.BGException;
import ru.bgcrm.plugin.bgbilling.Plugin;
import ru.bgcrm.plugin.bgbilling.proto.dao.VoiceIpDAO;
import ru.bgcrm.plugin.bgbilling.struts.action.BaseAction;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path = "/user/plugin/bgbilling/proto/voiceip")
public class VoiceIpAction extends BaseAction {
    private static final String PATH_JSP = Plugin.PATH_JSP_USER + "/voiceip";

    public ActionForward contractInfo(DynActionForm form, ConnectionSet conSet) throws BGException {
        String billingId = form.getParam("billingId");
        int contractId = form.getParamInt("contractId");

        VoiceIpDAO voipDAO = new VoiceIpDAO(form.getUser(), billingId, form.getParamInt("moduleId"));
        form.getResponse().setData("logins", voipDAO.getVoiceIpLogins(contractId));

        return html(conSet, form, PATH_JSP + "/contract.jsp");
    }

    public ActionForward getLogin(DynActionForm form, ConnectionSet conSet) throws Exception {
        String billingId = form.getParam("billingId");
        int loginId = form.getParamInt("loginId");

        VoiceIpDAO voipDAO = new VoiceIpDAO(form.getUser(), billingId);
        form.getResponse().setData("login", voipDAO.getVoiceIpLogin(loginId));

        return json(conSet, form);
    }

    public ActionForward updateLogin(DynActionForm form, ConnectionSet conSet) throws BGException {
        String billingId = form.getParam("billingId");

        Date dateFrom, dateTo = null;
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
        try {
            dateFrom = format.parse(Utils.maskNull(form.getParam("dateFrom")));
        } catch (ParseException e) {
            dateFrom = new Date();
        }
        try {
            dateTo = format.parse(Utils.maskNull(form.getParam("dateTo")));
        } catch (ParseException e) {
            dateTo = null;
        }

        int loginId = form.getParamInt("loginId", 0);
        if (loginId < 0) {
            loginId = 0;
        }

        VoiceIpDAO voipDAO = new VoiceIpDAO(form.getUser(), billingId);
        voipDAO.updateVoiceIpLogin(form.getParamInt("contractId"), loginId, form.getParam("alias"),
                form.getParamInt("objectId"), form.getParam("comment"), dateFrom, dateTo, form.getParam("type"),
                form.getParam("loginPassword"), form.getParamBoolean("setPassword", false));

        return json(conSet, form);
    }
}
