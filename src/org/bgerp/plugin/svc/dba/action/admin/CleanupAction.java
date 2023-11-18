package org.bgerp.plugin.svc.dba.action.admin;

import java.util.ArrayList;
import java.util.List;

import org.apache.struts.action.ActionForward;
import org.bgerp.plugin.svc.dba.Plugin;

import ru.bgcrm.plugin.PluginManager;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.action.BaseAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path = "/admin/plugin/dba/cleanup")
public class CleanupAction extends BaseAction {
    private static final String PATH_JSP = Plugin.PATH_JSP_ADMIN;

    @Override
    public ActionForward unspecified(DynActionForm form, ConnectionSet conSet) throws Exception {
        List<String> inconsistencyCleanupQueries = new ArrayList<>(100);

        for (var p : PluginManager.getInstance().getFullSortedPluginList()) {
            var cs = p.getCleaner();
            if (cs == null)
                continue;

            inconsistencyCleanupQueries.addAll(cs.inconsistencyCleanupQueries());
        }

        form.setResponseData("inconsistencyCleanupQueries", inconsistencyCleanupQueries);

        return html(conSet, form, PATH_JSP + "/cleanup.jsp");
    }

    public ActionForward queryDryRun(DynActionForm form, ConnectionSet conSet) throws Exception {
        String query = form.getParam("query", Utils::notBlankString);

        var con = conSet.getConnection();
        int rows = con.createStatement().executeUpdate(query);

        con.rollback();

        form.setResponseData("rows", rows);

        return html(conSet, form, PATH_JSP + "/cleanup_dry_run.jsp");
    }

    public ActionForward queryRun(DynActionForm form, ConnectionSet conSet) throws Exception {
        int updated = 0;

        for (String query : form.getParamValuesListStr("query")) {
            try (var st = conSet.getConnection().createStatement()) {
                updated += st.executeUpdate(query);
            }
        }

        form.setResponseData("updated", updated);

        return json(conSet, form);
    }
}
