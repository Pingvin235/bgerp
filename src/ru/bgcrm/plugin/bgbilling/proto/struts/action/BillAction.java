package ru.bgcrm.plugin.bgbilling.proto.struts.action;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForward;
import org.bgerp.app.exception.BGException;
import org.bgerp.model.Pageable;

import ru.bgcrm.plugin.bgbilling.Plugin;
import ru.bgcrm.plugin.bgbilling.proto.dao.BillDAO;
import ru.bgcrm.plugin.bgbilling.proto.model.bill.Bill;
import ru.bgcrm.plugin.bgbilling.proto.model.bill.Invoice;
import ru.bgcrm.plugin.bgbilling.struts.action.BaseAction;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path = "/user/plugin/bgbilling/proto/bill")
public class BillAction extends BaseAction {
    private static final String PATH_JSP = Plugin.PATH_JSP_USER + "/bill";

    public ActionForward attributeList(DynActionForm form, ConnectionSet conSet) {
        String billingId = form.getParam("billingId");
        int contractId = form.getParamInt("contractId");
        int moduleId = form.getParamInt("moduleId");

        form.getResponse().setData("list",
                new BillDAO(form.getUser(), billingId, moduleId).getAttributeList(contractId));

        return html(conSet, form, PATH_JSP + "/attribute_list.jsp");
    }

    public ActionForward docTypeList(DynActionForm form, ConnectionSet conSet) {
        String billingId = form.getParam("billingId");
        int contractId = form.getParamInt("contractId");
        int moduleId = form.getParamInt("moduleId");

        BillDAO billDao = new BillDAO(form.getUser(), billingId, moduleId);
        form.getResponse().setData("billTypeList", billDao.getContractDocTypeList(contractId, "bill"));
        form.getResponse().setData("invoiceTypeList", billDao.getContractDocTypeList(contractId, "invoice"));

        return html(conSet, form, PATH_JSP + "/doc_type_list.jsp");
    }

    public ActionForward docTypeAdd(DynActionForm form, ConnectionSet conSet) {
        String billingId = form.getParam("billingId");
        int contractId = form.getParamInt("contractId");
        int moduleId = form.getParamInt("moduleId");

        new BillDAO(form.getUser(), billingId, moduleId).contractDocTypeAdd(contractId, form.getParam("typeIds"));

        return json(conSet, form);
    }

    public ActionForward docTypeDelete(DynActionForm form, ConnectionSet conSet) {
        String billingId = form.getParam("billingId");
        int contractId = form.getParamInt("contractId");
        int moduleId = form.getParamInt("moduleId");

        new BillDAO(form.getUser(), billingId, moduleId).contractDocTypeDelete(contractId, form.getParam("typeIds"));

        return json(conSet, form);
    }

    public ActionForward documentList(DynActionForm form, ConnectionSet conSet) {
        String billingId = form.getParam("billingId");
        int contractId = form.getParamInt("contractId");
        int moduleId = form.getParamInt("moduleId");
        String mode = form.getParam("mode", "bill");
        form.setParam("mode", mode);

        BillDAO billDao = new BillDAO(form.getUser(), billingId, moduleId);
        if ("bill".equals(mode)) {
            billDao.searchBillList(contractId, new Pageable<Bill>(form));
        } else {
            billDao.searchInvoiceList(contractId, new Pageable<Invoice>(form));
        }

        return html(conSet, form, PATH_JSP + "/document_list.jsp");
    }

    public ActionForward getDocument(DynActionForm form, ConnectionSet conSet) {
        String billingId = form.getParam("billingId");
        int moduleId = form.getParamInt("moduleId");
        String type = form.getParam("type");
        String ids = form.getParam("ids");

        try {
            HttpServletResponse response = form.getHttpResponse();
            OutputStream out = response.getOutputStream();
            Utils.setFileNameHeaders(response, type + ".pdf");
            out.write(new BillDAO(form.getUser(), billingId, moduleId).getDocumentsPdf(ids, type));
        } catch (Exception ex) {
            throw new BGException(ex);
        }

        return null;
    }

    public ActionForward setPayed(DynActionForm form, ConnectionSet conSet) {
        String billingId = form.getParam("billingId");
        int moduleId = form.getParamInt("moduleId");

        String ids = form.getParam("ids");
        Date date = form.getParamDate("date");
        BigDecimal summa = Utils.parseBigDecimal(form.getParam("summa"));
        String comment = form.getParam("comment");

        BillDAO billDao = new BillDAO(form.getUser(), billingId, moduleId);
        if (date != null) {
            billDao.setPayed(ids, true, date, summa, comment);
        } else {
            billDao.setPayed(ids, false, null, null, null);
        }

        return json(conSet, form);
    }
}