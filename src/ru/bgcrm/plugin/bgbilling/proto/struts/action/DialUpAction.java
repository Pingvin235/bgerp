package ru.bgcrm.plugin.bgbilling.proto.struts.action;

import java.util.ArrayList;
import java.util.List;

import org.apache.struts.action.ActionForward;

import ru.bgcrm.model.BGException;
import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.plugin.bgbilling.Plugin;
import ru.bgcrm.plugin.bgbilling.proto.dao.ContractObjectDAO;
import ru.bgcrm.plugin.bgbilling.proto.dao.DialUpDAO;
import ru.bgcrm.plugin.bgbilling.proto.model.dialup.DialUpError;
import ru.bgcrm.plugin.bgbilling.proto.model.dialup.DialUpLogin;
import ru.bgcrm.plugin.bgbilling.proto.model.dialup.DialUpLoginAttr;
import ru.bgcrm.plugin.bgbilling.proto.model.dialup.DialUpLoginAttrSet;
import ru.bgcrm.plugin.bgbilling.proto.model.dialup.DialUpLoginIp;
import ru.bgcrm.plugin.bgbilling.proto.model.dialup.DialUpLoginRadiusInfo;
import ru.bgcrm.plugin.bgbilling.struts.action.BaseAction;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path = "/user/plugin/bgbilling/proto/dialup")
public class DialUpAction extends BaseAction {
    private static final String PATH_JSP = Plugin.PATH_JSP_USER + "/dialup";

    public ActionForward contractInfo(DynActionForm form, ConnectionSet conSet) throws Exception {
        DialUpDAO dialUpDAO = new DialUpDAO(form.getUser(), form.getParam("billingId"), form.getParamInt("moduleId"));
        int contractId = form.getParamInt("contractId");

        form.getResponse().setData("loginList", dialUpDAO.getLoginList(contractId));
        form.getResponse().setData("periodList", dialUpDAO.getCalculatePeriodList(contractId));

        return html(conSet, form, PATH_JSP + "/contract.jsp");
    }

    public ActionForward getLogin(DynActionForm form, ConnectionSet conSet) throws Exception {
        int contractId = form.getParamInt("contractId");

        DialUpDAO dialUpDAO = new DialUpDAO(form.getUser(), form.getParam("billingId"), form.getParamInt("moduleId"));

        if (form.getId() > 0) {
            form.getResponse().setData("login", dialUpDAO.getLogin(form.getId()));
            form.getResponse().setData("ipAddressList", dialUpDAO.getLoginIPAddress(form.getId()));
            form.getResponse().setData("pswdLog", dialUpDAO.getLoginPasswordLog(form.getId()));
            form.getResponse().setData("radiusInfo", dialUpDAO.getLoginRadiusInfo(form.getId()));
        }

        form.getResponse().setData("attrSetList", dialUpDAO.getAttrSetList());
        form.getResponse().setData("attrTypeList", dialUpDAO.getAttrTypeList());
        form.getResponse().setData("realmList", dialUpDAO.getRealmList());
        form.getResponse().setData("realmGroupList", dialUpDAO.getRealmGroupList());

        form.getResponse().setData("objectList",
                new ContractObjectDAO(form.getUser(), form.getParam("billingId")).getContractObjects(contractId));

        return html(conSet, form, PATH_JSP + "/login_editor.jsp");
    }

    public ActionForward updateLogin(DynActionForm form, ConnectionSet conSet) throws Exception {
        int contractId = form.getParamInt("contractId");

        DialUpLogin login = new DialUpLogin();
        login.setId(form.getId());
        login.setStatus(form.getParamInt("status", DialUpLogin.STATUS_ACTIVE));
        login.setAlias(form.getParam("alias"));
        login.setObjectId(form.getParamInt("objectId"));
        login.setDateFrom(form.getParamDate("dateFrom"));
        login.setDateTo(form.getParamDate("dateTo"));
        login.setSession(form.getParamInt("sessions"));
        login.setComment(form.getParam("comment"));

        String pswd1 = form.getParam("pswd1", "");
        String pswd2 = form.getParam("pswd2", "");

        String pswd = null;
        boolean pswdSet = Utils.parseBoolean(form.getParam("pswdSet"));

        if (!pswd1.equals(pswd2)) {
            throw new BGMessageException("Пароли не совпадают!");
        }

        if (!pswd1.equals("*******")) {
            pswd = pswd1;
        }

        DialUpDAO dialUpDAO = new DialUpDAO(form.getUser(), form.getParam("billingId"), form.getParamInt("moduleId"));
        dialUpDAO.updateLogin(contractId, login, pswd, pswdSet);

        return json(conSet, form);
    }

    public ActionForward getLoginPassword(DynActionForm form, ConnectionSet conSet) throws Exception {

        String billingId = form.getParam("billingId");
        int moduleId = form.getParamInt("moduleId");
        int contractId = form.getParamInt("contractId");
        int login = form.getParamInt("login");

        DialUpDAO dialUpDAO = new DialUpDAO(form.getUser(), billingId, moduleId);
        form.getResponse().setData("password", dialUpDAO.getLoginPassword(contractId, login));

        return json(conSet, form);
    }

    public ActionForward updateLoginPassword(DynActionForm form, ConnectionSet conSet) throws Exception {
        int loginId = form.getParamInt("loginId");

        DialUpDAO dialUpDAO = new DialUpDAO(form.getUser(), form.getParam("billingId"), form.getParamInt("moduleId"));
        dialUpDAO.updatePassword(loginId);

        return json(conSet, form);
    }

    public ActionForward updateStaticIP(DynActionForm form, ConnectionSet conSet) throws Exception {
        DialUpDAO dialUpDAO = new DialUpDAO(form.getUser(), form.getParam("billingId"), form.getParamInt("moduleId"));

        List<DialUpLoginIp> addrList = new ArrayList<DialUpLoginIp>();
        for (String token : form.getSelectedValuesListStr("address")) {
            String[] parts = token.split(":", -1);
            if (parts.length != 3) {
                throw new BGException("Incorrect token: " + token);
            }

            DialUpLoginIp addr = new DialUpLoginIp();
            addr.setAddress(parts[0]);
            addr.setRealm(parts[1]);
            TimeUtils.parsePeriod(parts[2], addr);

            addrList.add(addr);
        }

        dialUpDAO.updateLoginIpAddress(form.getId(), addrList);

        return json(conSet, form);
    }

    public ActionForward updateLoginRadiusInfo(DynActionForm form, ConnectionSet conSet) throws Exception {
        DialUpDAO dialUpDAO = new DialUpDAO(form.getUser(), form.getParam("billingId"), form.getParamInt("moduleId"));

        DialUpLoginRadiusInfo radiusInfo = new DialUpLoginRadiusInfo();
        radiusInfo.setAttributeMode(form.getParamInt("attributeMode"));
        radiusInfo.setRealmGroup(form.getParam("realmGroup", ""));

        for (String token : form.getSelectedValuesListStr("attrSet")) {
            String[] parts = token.split(":", -1);
            if (parts.length != 2) {
                throw new BGException("Incorrect token: " + token);
            }

            DialUpLoginAttrSet set = new DialUpLoginAttrSet();
            set.setId(Utils.parseInt(parts[0]));
            set.setRealm(parts[1]);
            radiusInfo.addAttrSet(set);
        }

        for (String token : form.getSelectedValuesListStr("attribute")) {
            String[] parts = token.split(":", -1);
            if (parts.length != 3) {
                throw new BGException("Incorrect token: " + token);
            }

            DialUpLoginAttr attr = new DialUpLoginAttr();
            attr.setName(parts[0]);
            attr.setValue(parts[1]);
            attr.setRealm(parts[2]);
            radiusInfo.addAttr(attr);
        }

        dialUpDAO.updateLoginRadiusInfo(form.getId(), radiusInfo);

        return json(conSet, form);
    }

    public ActionForward getPeriod(DynActionForm form, ConnectionSet conSet) throws Exception {
        int contractId = form.getParamInt("contractId");

        if (form.getId() > 0) {
            DialUpDAO dialUpDAO = new DialUpDAO(form.getUser(), form.getParam("billingId"),
                    form.getParamInt("moduleId"));
            form.getResponse().setData("period", dialUpDAO.getCalculatePeriod(contractId, form.getId()));
        }

        return html(conSet, form, PATH_JSP + "/period_editor.jsp");
    }

    public ActionForward updatePeriod(DynActionForm form, ConnectionSet conSet) throws Exception {

        int contractId = form.getParamInt("contractId");

        DialUpDAO dialUpDAO = new DialUpDAO(form.getUser(), form.getParam("billingId"), form.getParamInt("moduleId"));
        dialUpDAO.updatePeriod(contractId, form.getParamDate("dateFrom"), form.getParamDate("dateTo"), form.getId());

        return json(conSet, form);
    }

    public ActionForward sessionList(DynActionForm form, ConnectionSet conSet) throws Exception {

        String billingId = form.getParam("billingId");
        int contractId = form.getParamInt("contractId");
        int loginId = form.getParamInt("loginId");
        int sessionDays = form.getParamInt("sessionDays");
        int moduleId = form.getParamInt("moduleId");

        DialUpDAO dialUpDAO = new DialUpDAO(form.getUser(), billingId, moduleId);

        form.getResponse().setData("activeSessionList", dialUpDAO.getActiveSessionList(contractId, loginId));
        form.getResponse().setData("sessionList", dialUpDAO.getSessionList(contractId, loginId, sessionDays));

        return html(conSet, form, PATH_JSP + "/dialup_session_list.jsp");
    }

    public ActionForward errorList(DynActionForm form, ConnectionSet conSet) throws Exception {
        int contractId = form.getParamInt("contractId");
        int loginId = form.getParamInt("loginId");
        int errorDays = form.getParamInt("errorDays");

        DialUpDAO dialUpDAO = new DialUpDAO(form.getUser(), form.getParam("billingId"), form.getParamInt("moduleId"));
        List<DialUpError> errorList = dialUpDAO.getErrorList(contractId, loginId, errorDays);

        form.getResponse().setData("errorList", errorList);

        return html(conSet, form, PATH_JSP + "/dialup_error_list.jsp");
    }

    public ActionForward radiusLog(DynActionForm form, ConnectionSet conSet) throws Exception {

        String splitter = form.getParam("splitter");
        String sessionId = form.getParam("sessionId");
        String sessionStart = form.getParam("sessionStart");

        DialUpDAO dialUpDAO = new DialUpDAO(form.getUser(), form.getParam("billingId"), form.getParamInt("moduleId"));
        String radiusLog = dialUpDAO.getRadiusLog(sessionId, sessionStart, splitter);

        form.getResponse().setData("radiusLog", radiusLog);

        return html(conSet, form, PATH_JSP + "/radius_log.jsp");
    }

    public ActionForward terminateSession(DynActionForm form, ConnectionSet conSet) throws Exception {
        String recordId = form.getParam("recordId");

        DialUpDAO dialUpDAO = new DialUpDAO(form.getUser(), form.getParam("billingId"), form.getParamInt("moduleId"));
        dialUpDAO.terminateSession(recordId);

        return json(conSet, form);
    }
}