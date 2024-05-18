package org.bgerp.plugin.svc.dba.action.admin;

import org.apache.struts.action.ActionForward;
import org.bgerp.action.BaseAction;
import org.bgerp.app.exception.BGException;
import org.bgerp.plugin.svc.dba.Plugin;
import org.bgerp.plugin.svc.dba.dao.QueryDAO;
import org.bgerp.plugin.svc.dba.dao.QueryHistoryDAO;
import org.bgerp.plugin.svc.dba.model.QueryTable;
import org.bgerp.plugin.svc.dba.model.QueryType;

import ru.bgcrm.model.user.PermissionActionMethodException;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path = "/admin/plugin/dba/query")
public class QueryAction extends BaseAction {
    private static final String PATH_JSP = Plugin.PATH_JSP_ADMIN;

    @Override
    public ActionForward unspecified(DynActionForm form, ConnectionSet conSet) throws Exception {
        QueryDAO qDao = new QueryDAO(conSet);
        QueryHistoryDAO hDao = new QueryHistoryDAO(conSet.getConnection());

        String query = form.getParam("query");
        if (Utils.notBlankString(query)) {
            QueryType type = QueryType.of(query);
            if (type == null)
                throw new BGException("Unknown query type");
            permissionCheck(form, type);

            // first request, not a page change
            if (form.getPage().getPageSize() == 0)
                hDao.update(form.getUserId(), query);

            var table = new QueryTable(form);
            qDao.query(table, type, query);
            form.setResponseData("table", table);
        }

        form.setResponseData("storedQueries", hDao.list(form.getUserId()));

        return html(conSet, form, PATH_JSP + "/query.jsp");
    }

    private void permissionCheck(DynActionForm form, QueryType type) throws Exception {
        String className = this.getClass().getName();
        permissionCheck(form, className + ":query" + type.prefix().substring(0, 1) + type.prefix().substring(1).toLowerCase());
    }

    public ActionForward querySelect(DynActionForm form, ConnectionSet conSet) {
        throw new PermissionActionMethodException();
    }

    public ActionForward queryShow(DynActionForm form, ConnectionSet conSet) {
        throw new PermissionActionMethodException();
    }

    public ActionForward queryInsert(DynActionForm form, ConnectionSet conSet) {
        throw new PermissionActionMethodException();
    }

    public ActionForward queryCreate(DynActionForm form, ConnectionSet conSet) {
        throw new PermissionActionMethodException();
    }

    public ActionForward queryUpdate(DynActionForm form, ConnectionSet conSet) {
        throw new PermissionActionMethodException();
    }

    public ActionForward queryDelete(DynActionForm form, ConnectionSet conSet) {
        throw new PermissionActionMethodException();
    }

    public ActionForward queryDrop(DynActionForm form, ConnectionSet conSet) {
        throw new PermissionActionMethodException();
    }
}
