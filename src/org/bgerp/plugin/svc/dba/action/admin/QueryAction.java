package org.bgerp.plugin.svc.dba.action.admin;

import java.sql.SQLException;

import org.apache.struts.action.ActionForward;
import org.bgerp.plugin.svc.dba.Plugin;
import org.bgerp.plugin.svc.dba.dao.QueryDAO;
import org.bgerp.plugin.svc.dba.model.QueryTable;
import org.bgerp.plugin.svc.dba.model.QueryType;

import ru.bgcrm.model.BGException;
import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.model.user.PermissionActionMethodException;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.action.BaseAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path = "/admin/plugin/dba/query")
public class QueryAction extends BaseAction {
    private static final String PATH_JSP = Plugin.PATH_JSP_ADMIN;

    @Override
    public ActionForward unspecified(DynActionForm form, ConnectionSet conSet) throws Exception {
        String query = form.getParam("query");
        if (Utils.notBlankString(query)) {
            QueryType type = QueryType.of(query);
            if (type == null)
                throw new BGException("Unknown query type");
            permissionCheck(form, type);

            var table = new QueryTable(form);
            try {
                new QueryDAO(conSet).query(table, type, query);
            } catch (SQLException e) {
                throw new BGMessageException(e.getMessage());
            }
            form.setResponseData("table", table);
        }

        return html(conSet, form, PATH_JSP + "/query.jsp");
    }

    private void permissionCheck(DynActionForm form, QueryType type) throws BGException {
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