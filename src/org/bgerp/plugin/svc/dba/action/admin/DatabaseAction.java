package org.bgerp.plugin.svc.dba.action.admin;

import java.time.YearMonth;

import org.apache.struts.action.ActionForward;
import org.bgerp.action.base.BaseAction;
import org.bgerp.plugin.svc.dba.Config;
import org.bgerp.plugin.svc.dba.Plugin;
import org.bgerp.plugin.svc.dba.dao.DatabaseDAO;

import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path = "/admin/plugin/dba/db")
public class DatabaseAction extends BaseAction {
    private static final String PATH_JSP = Plugin.PATH_JSP_ADMIN;

    @Override
    public ActionForward unspecified(DynActionForm form, ConnectionSet conSet) throws Exception {
        var tables = new DatabaseDAO(conSet.getConnection()).tables();

        setup.getConfig(Config.class).dropCandidates(tables, YearMonth.now());

        Long rows = tables.stream()
            .map(status -> status.getRows())
            .reduce(0L, Long::sum);
        form.setResponseData("rows", rows);

        Long size = tables.stream()
            .map(status -> status.getDataLength() + status.getIndexLength())
            .reduce(0L, Long::sum);
        form.setResponseData("size", size);

        Integer dropCandidateCnt = tables.stream()
            .map(status -> status.isDropCandidate() ? 1 : 0)
            .reduce(0, Integer::sum);
        form.setResponseData("dropCandidateCnt", dropCandidateCnt);

        form.setResponseData("tables", tables);

        return html(conSet, form, PATH_JSP + "/database.jsp");
    }

    public ActionForward tableDrop(DynActionForm form, ConnectionSet conSet) throws Exception {
        var tables = form.getParamValuesListStr("table");

        try (var st = conSet.getConnection().createStatement()) {
            for (String table : tables)
                st.executeUpdate("DROP TABLE " + table);
        }

        return unspecified(form, conSet);
    }
}
