package ru.bgcrm.plugin.bgbilling.proto.struts.action;

import java.util.Date;

import org.apache.struts.action.ActionForward;

import ru.bgcrm.model.BGException;
import ru.bgcrm.plugin.bgbilling.Plugin;
import ru.bgcrm.plugin.bgbilling.proto.dao.IpnDAO;
import ru.bgcrm.plugin.bgbilling.proto.model.ipn.IpnRange;
import ru.bgcrm.plugin.bgbilling.struts.action.BaseAction;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.struts.form.Response;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path = "/user/plugin/bgbilling/proto/ipn")
public class IpnAction extends BaseAction {
    private static final String PATH_JSP = Plugin.PATH_JSP_USER + "/ipn";

    public ActionForward rangeList(DynActionForm form, ConnectionSet conSet) throws BGException {
        String billingId = form.getParam("billingId");
        int contractId = form.getParamInt("contractId");
        int moduleId = form.getParamInt("moduleId");
        Date date = form.getParamDate("date");

        IpnDAO ipnDao = new IpnDAO(form.getUser(), billingId, moduleId);
        form.getResponse().setData("rangeList", ipnDao.getIpnRanges(contractId, date, false));
        form.getResponse().setData("netList", ipnDao.getIpnRanges(contractId, date, true));

        return html(conSet, form, PATH_JSP + "/range_list.jsp");
    }

    public ActionForward rangeEdit(DynActionForm form, ConnectionSet conSet) throws BGException {
        String billingId = form.getParam("billingId");
        int contractId = form.getParamInt("contractId");
        int moduleId = form.getParamInt("moduleId");

        IpnDAO ipnDao = new IpnDAO(form.getUser(), billingId, moduleId);
        form.getResponse().setData("range", ipnDao.getIpnRange(contractId, form.getId()));
        form.getResponse().setData("sourceList", ipnDao.getSourceList(new Date()));
        form.getResponse().setData("planList", ipnDao.linkPlanList());

        return html(conSet, form, PATH_JSP + "/range_edit.jsp");
    }

    public ActionForward rangeDelete(DynActionForm form, ConnectionSet conSet) throws BGException {
        String billingId = form.getParam("billingId");
        int contractId = form.getParamInt("contractId");
        int moduleId = form.getParamInt("moduleId");

        new IpnDAO(form.getUser(), billingId, moduleId).deleteIpnRange(contractId, form.getId());

        return json(conSet, form);
    }

    public ActionForward rangeUpdate(DynActionForm form, ConnectionSet conSet) throws BGException {
        String billingId = form.getParam("billingId");
        int contractId = form.getParamInt("contractId");
        int moduleId = form.getParamInt("moduleId");

        IpnDAO ipnDao = new IpnDAO(form.getUser(), billingId, moduleId);

        IpnRange range = new IpnRange();
        range.setId(form.getId());
        range.setContractId(contractId);
        range.setAddressFrom(form.getParam("addressFrom"));
        range.setAddressTo(form.getParam("addressTo"));
        range.setMask(form.getParamInt("mask"));
        range.setDateFrom(form.getParamDate("dateFrom"));
        range.setDateTo(form.getParamDate("dateTo"));
        range.setIfaceList(form.getSelectedValuesListStr("iface"));
        range.setPlan(form.getParamInt("plan"));
        range.setComment(form.getParam("comment"));

        ipnDao.updateIpnRange(range);

        return json(conSet, form);
    }

    public ActionForward gateStatus(DynActionForm form, ConnectionSet conSet) throws BGException {
        String billingId = form.getParam("billingId");
        int contractId = form.getParamInt("contractId");
        int moduleId = form.getParamInt("moduleId");

        IpnDAO ipnDao = new IpnDAO(form.getUser(), billingId, moduleId);
        form.getResponse().setData("info", ipnDao.gateInfo(contractId));

        return html(conSet, form, PATH_JSP + "/gate_status.jsp");
    }

    public ActionForward gateStatusUpdate(DynActionForm form, ConnectionSet conSet) throws BGException {
        String billingId = form.getParam("billingId");
        int contractId = form.getParamInt("contractId");
        int moduleId = form.getParamInt("moduleId");

        IpnDAO ipnDao = new IpnDAO(form.getUser(), billingId, moduleId);
        ipnDao.gateStatusUpdate(contractId, form.getParamInt("status"));

        return json(conSet, form);
    }

    public ActionForward gateRuleEdit(DynActionForm form, ConnectionSet conSet) throws BGException {
        String billingId = form.getParam("billingId");
        int contractId = form.getParamInt("contractId");
        int moduleId = form.getParamInt("moduleId");

        int gateTypeId = form.getParamInt("gateTypeId");
        int gateId = form.getParamInt("gateId");

        IpnDAO ipnDao = new IpnDAO(form.getUser(), billingId, moduleId);

        Response resp = form.getResponse();
        if (gateId > 0) {
            resp.setData("ruleTypeList", ipnDao.gateRuleTypeList(gateTypeId));
            if (form.getId() > 0) {
                resp.setData("rulePair", ipnDao.getUserGateRule(form.getId()));
            }

            resp.setData("rangeList", ipnDao.getIpnRanges(contractId, form.getParamDate("date"), false));
            resp.setData("netList", ipnDao.getIpnRanges(contractId, form.getParamDate("date"), true));

            return html(conSet, form, PATH_JSP + "/gate_edit.jsp");
        } else {
            resp.setData("gateList", ipnDao.getGateList());

            return html(conSet, form, PATH_JSP + "/gate_select.jsp");
        }
    }

    public ActionForward gateRuleGenerate(DynActionForm form, ConnectionSet conSet) throws BGException {
        String billingId = form.getParam("billingId");
        // int contractId = form.getParamInt( "contractId" );
        int moduleId = form.getParamInt("moduleId");
        // Date date = form.getParamDate( "date" );

        IpnDAO ipnDao = new IpnDAO(form.getUser(), billingId, moduleId);

        String rule = ipnDao.generateRule(form.getParamInt("ruleTypeId"), form.getParamInt("gateTypeId"),
                form.getParam("addressList"));
        form.getResponse().setData("rule", rule);

        return json(conSet, form);
    }

    public ActionForward gateRuleUpdate(DynActionForm form, ConnectionSet conSet) throws BGException {
        String billingId = form.getParam("billingId");
        int contractId = form.getParamInt("contractId");
        int moduleId = form.getParamInt("moduleId");

        new IpnDAO(form.getUser(), billingId, moduleId).updateGateRule(contractId, form.getId(),
                form.getParamInt("gateId"), form.getParamInt("ruleTypeId"), form.getParam("rule", ""));

        return json(conSet, form);
    }

    public ActionForward gateRuleDelete(DynActionForm form, ConnectionSet conSet) throws BGException {
        String billingId = form.getParam("billingId");
        int contractId = form.getParamInt("contractId");
        int moduleId = form.getParamInt("moduleId");

        new IpnDAO(form.getUser(), billingId, moduleId).deleteGateRule(contractId, form.getId());

        return json(conSet, form);
    }

    private long ipToLong(String ipAddress) {
        long result = 0;

        String[] ipAddressInArray = ipAddress.split("\\.");
        for (int i = 3; i >= 0; i--) {
            long ip = Long.parseLong(ipAddressInArray[3 - i]);
            result |= ip << (i * 8);
        }

        return result;
    }

    public ActionForward findAddress(DynActionForm form, ConnectionSet conSet) throws BGException {
        String billingId = form.getParam("billingId");
        int moduleId = form.getParamInt("moduleId");

        long address = ipToLong(form.getParam("address"));
        int mask = form.getParamInt("mask");
        int port = form.getParamInt("port");
        Date dateFrom = form.getParamDate("dateFrom");
        Date dateTo = form.getParamDate("dateTo");
        String comment = form.getParam("comment");

        form.getResponse().setData("addresses", new IpnDAO(form.getUser(), billingId, moduleId)
                .findAddress(form.getPage(), address, mask, port, dateFrom, dateTo, comment));

        return json(conSet, form);
    }
}
