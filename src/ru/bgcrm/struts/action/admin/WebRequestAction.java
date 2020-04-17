package ru.bgcrm.struts.action.admin;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.bgcrm.dao.WebRequestLogDAO;
import ru.bgcrm.model.LogEntry;
import ru.bgcrm.model.SearchResult;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.sql.ConnectionSet;

public class WebRequestAction extends ru.bgcrm.struts.action.BaseAction {
    public WebRequestAction() {
        super();
    }

    @Override
    protected ActionForward unspecified(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws Exception {
        return processUserTypedForward(conSet, mapping, form, FORWARD_DEFAULT);
    }

    public ActionForward findRequests(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws Exception {
        SearchResult<LogEntry> searchResult = new SearchResult<LogEntry>(form);

        WebRequestLogDAO webRequestLogDAO = new WebRequestLogDAO(conSet.getConnection());
        webRequestLogDAO.serchRequest(searchResult, form);

        return processUserTypedForward(conSet, mapping, form, "findRequests");
    }
}
