package ru.bgcrm.plugin.bgbilling.proto.struts.action;

import org.apache.struts.action.ActionForward;

import ru.bgcrm.plugin.bgbilling.Plugin;
import ru.bgcrm.plugin.bgbilling.proto.dao.ContractObjectDAO;
import ru.bgcrm.plugin.bgbilling.proto.dao.DirectoryDAO;
import ru.bgcrm.plugin.bgbilling.proto.dao.NPayDAO;
import ru.bgcrm.plugin.bgbilling.proto.model.npay.NPayService;
import ru.bgcrm.plugin.bgbilling.struts.action.BaseAction;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path = "/user/plugin/bgbilling/proto/npay")
public class NPayAction extends BaseAction {
    private static final String PATH_JSP = Plugin.PATH_JSP_USER + "/npay";

    public ActionForward serviceList(DynActionForm form, ConnectionSet conSet) throws Exception {
        String billingId = form.getParam("billingId");
        int contractId = form.getParamInt("contractId");
        int moduleId = form.getParamInt("moduleId");

        form.getResponse().setData("list",
                NPayDAO.getInstance(form.getUser(), billingId, moduleId).getServiceList(contractId));

        return html(conSet, form, PATH_JSP + "/service_list.jsp");
    }

    public ActionForward serviceGet(DynActionForm form, ConnectionSet conSet) throws Exception {
        String billingId = form.getParam("billingId");
        int contractId = form.getParamInt("contractId");
        int moduleId = form.getParamInt("moduleId");

        if (form.getId() > 0) {
            form.getResponse().setData("service",
                    NPayDAO.getInstance(form.getUser(), billingId, moduleId).getService(form.getId()));
        }
        form.getResponse().setData("serviceTypeList",
                new DirectoryDAO(form.getUser(), billingId).getServiceTypeList(moduleId));
        form.getResponse().setData("objectList",
                new ContractObjectDAO(form.getUser(), billingId).getContractObjects(contractId));

        return html(conSet, form, PATH_JSP + "/service_editor.jsp");
    }

    public ActionForward serviceUpdate(DynActionForm form, ConnectionSet conSet) throws Exception {
        String billingId = form.getParam("billingId");
        int contractId = form.getParamInt("contractId");
        int moduleId = form.getParamInt("moduleId");

        NPayService service = new NPayService();
        service.setId(form.getId());
        service.setContractId(contractId);
        service.setServiceId(form.getParamInt("serviceId"));
        service.setDateFrom(form.getParamDate("dateFrom"));
        service.setDateTo(form.getParamDate("dateTo"));
        service.setObjectId(form.getParamInt("objectId"));
        service.setComment(form.getParam("comment", ""));

        NPayDAO.getInstance(form.getUser(), billingId, moduleId).updateService(service);

        return json(conSet, form);
    }

    public ActionForward serviceDelete(DynActionForm form, ConnectionSet conSet) throws Exception {
        String billingId = form.getParam("billingId");
        int moduleId = form.getParamInt("moduleId");

        NPayDAO.getInstance(form.getUser(), billingId, moduleId).deleteService(form.getId());

        return json(conSet, form);
    }
}