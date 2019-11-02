package ru.bgcrm.plugin.fulltext.struts.action;

import java.sql.Connection;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.bgcrm.model.BGException;
import ru.bgcrm.model.Customer;
import ru.bgcrm.model.SearchResult;
import ru.bgcrm.plugin.fulltext.dao.SearchDAO;
import ru.bgcrm.struts.action.BaseAction;
import ru.bgcrm.struts.form.DynActionForm;

public class SearchAction extends BaseAction {

    public ActionForward search(ActionMapping mapping, DynActionForm form, Connection con) throws BGException {
        String objectType = form.getParam("objectType");
        String filter = form.getParam("filter", "");
        
        SearchDAO searchDao = new SearchDAO(con);
        if (Customer.OBJECT_TYPE.equals(objectType)) {
            searchDao.searchCustomer(new SearchResult<Customer>(form), filter);
            return mapping.findForward(objectType);
        }
        
        return mapping.findForward(FORWARD_DEFAULT);
    }
    
}
