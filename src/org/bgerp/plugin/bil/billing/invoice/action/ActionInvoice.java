package org.bgerp.plugin.bil.billing.invoice.action;

import org.apache.struts.action.ActionForward;
import org.bgerp.plugin.bil.billing.invoice.Plugin;
import org.bgerp.plugin.bil.billing.invoice.dao.InvoiceSearchDAO;

import ru.bgcrm.model.SearchResult;
import ru.bgcrm.struts.action.BaseAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.sql.ConnectionSet;

public class ActionInvoice extends BaseAction {
    private static final String PATH_JSP = Plugin.PATH_JSP_USER;

    public ActionForward list(DynActionForm form, ConnectionSet conSet) throws Exception {
        new InvoiceSearchDAO(conSet.getSlaveConnection())
            .withProcessId(form.getParamInt("processId"))
            .search(new SearchResult<>(form));
        return html(conSet, form, PATH_JSP + "/list.jsp");
    }
}
