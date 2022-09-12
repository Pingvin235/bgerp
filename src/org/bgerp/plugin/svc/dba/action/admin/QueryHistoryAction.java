package org.bgerp.plugin.svc.dba.action.admin;

import java.sql.SQLException;
import java.util.List;

import org.apache.struts.action.ActionForward;
import org.bgerp.plugin.svc.dba.dao.QueryHistoryDAO;

import ru.bgcrm.model.IdTitle;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.action.BaseAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path = "/admin/plugin/dba/query/history")
public class QueryHistoryAction extends BaseAction {
    public ActionForward get(DynActionForm form, ConnectionSet conSet) throws SQLException {
        var query = new QueryHistoryDAO(conSet.getConnection()).get(form.getUserId(), form.getId());
        form.setResponseData("query", query);
        return json(conSet, form);
    }

    public ActionForward del(DynActionForm form, ConnectionSet conSet) throws SQLException {
        QueryHistoryDAO hDao = new QueryHistoryDAO(conSet.getConnection());
        hDao.delete(form.getUserId(), form.getId());
        List<IdTitle> storedQueries = hDao.list(form.getUserId());
        form.setResponseData("storedQueries", storedQueries);
        return json(conSet, form);
    }

}
