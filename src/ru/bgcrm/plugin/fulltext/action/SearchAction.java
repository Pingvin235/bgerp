package ru.bgcrm.plugin.fulltext.action;

import org.apache.struts.action.ActionForward;
import org.bgerp.model.Pageable;

import ru.bgcrm.model.Pair;
import ru.bgcrm.model.customer.Customer;
import ru.bgcrm.model.message.Message;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.plugin.fulltext.Plugin;
import ru.bgcrm.plugin.fulltext.dao.SearchDAO;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.action.BaseAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path = "/user/plugin/fulltext/search")
public class SearchAction extends BaseAction {
    private static final String PATH_JSP = PATH_JSP_USER + "/" + Plugin.ID;

    public ActionForward search(DynActionForm form, ConnectionSet conSet) throws Exception {
        String objectType = form.getParam("objectType");
        String filter = form.getParam("filter", "").trim();

        SearchDAO searchDao = new SearchDAO(conSet.getSlaveConnection());
        if (Customer.OBJECT_TYPE.equals(objectType)) {
            searchDao.searchCustomer(new Pageable<Customer>(form), filter);
            return forward(conSet, form, objectType);
        } else if (Process.OBJECT_TYPE.equals(objectType)) {
            searchDao.searchProcess(new Pageable<Process>(form), filter);
            return forward(conSet, form, objectType);
        } else if (Message.OBJECT_TYPE.equals(objectType)) {
            searchDao.searchMessages(new Pageable<Pair<Message, Process>>(form), filter);
            return forward(conSet, form, objectType);
        }

        return html(conSet, form, PATH_JSP + "/search.jsp");
    }

    private ActionForward forward(ConnectionSet conSet, DynActionForm form, String objectType) {
        return html(conSet, form, PATH_JSP + "/search_result_" + objectType + ".jsp");
    }
}
