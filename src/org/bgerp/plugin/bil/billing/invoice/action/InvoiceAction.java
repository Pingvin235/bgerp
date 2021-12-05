package org.bgerp.plugin.bil.billing.invoice.action;

import java.util.Objects;

import org.apache.struts.action.ActionForward;
import org.bgerp.plugin.bil.billing.invoice.Config;
import org.bgerp.plugin.bil.billing.invoice.Plugin;
import org.bgerp.plugin.bil.billing.invoice.dao.InvoiceDAO;
import org.bgerp.plugin.bil.billing.invoice.dao.InvoiceSearchDAO;

import javassist.NotFoundException;
import ru.bgcrm.model.SearchResult;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.action.BaseAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path = "/user/plugin/invoice/invoice")
public class InvoiceAction extends BaseAction {
    private static final String PATH_JSP = Plugin.PATH_JSP_USER;

    public ActionForward list(DynActionForm form, ConnectionSet conSet) throws Exception {
        new InvoiceSearchDAO(conSet.getSlaveConnection())
            .withProcessId(form.getParamInt("processId"))
            .search(new SearchResult<>(form));

        form.setRequestAttribute("config", setup.getConfig(Config.class));

        return html(conSet, form, PATH_JSP + "/process/list.jsp");
    }

    public ActionForward create(DynActionForm form, ConnectionSet conSet) throws Exception {
        int typeId = form.getParamInt("typeId");
        if (typeId > 0) {
            var type = setup.getConfig(Config.class).getType(typeId);

            int processId = form.getParamInt("processId", val -> val > 0);
            var month = form.getParamYearMonth("dateFrom", Objects::nonNull);

            new InvoiceDAO(conSet.getConnection()).update(type.invoice(processId, month));

            return json(conSet, form);
        } else {
            form.setRequestAttribute("types", setup.getConfig(Config.class).getTypes());
            return html(conSet, form, PATH_JSP + "/process/create.jsp");
        }
    }

    public ActionForward get(DynActionForm form, ConnectionSet conSet) throws Exception {
        var invoice = new InvoiceDAO(conSet.getConnection()).get(form.getId());
        form.setResponseData("invoice", invoice);
        return html(conSet, form, PATH_JSP + "/edit.jsp");
    }

    public ActionForward update(DynActionForm form, ConnectionSet conSet) throws Exception {
        //call provider

        // TODO: Parse update params. positions - from JSON.
        return json(conSet, form);
    }

    public ActionForward delete(DynActionForm form, ConnectionSet conSet) throws Exception {
        new InvoiceDAO(conSet.getConnection()).delete(form.getId());
        return json(conSet, form);
    }

    public ActionForward doc(DynActionForm form, ConnectionSet conSet) throws Exception {
        var invoice = new InvoiceDAO(conSet.getSlaveConnection()).get(form.getId());
        if (invoice == null)
            throw new NotFoundException("Not found invoice, id=" + form.getId());

        var config = setup.getConfig(Config.class);
        var type = config.getType(invoice.getTypeId());

        return html(conSet, form, type.getJsp());
    }
}
