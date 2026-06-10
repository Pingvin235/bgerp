package org.bgerp.plugin.report.action;

import org.apache.struts.action.ActionForward;
import org.bgerp.action.base.BaseAction;
import org.bgerp.plugin.report.Config;
import org.bgerp.plugin.report.Plugin;
import org.bgerp.plugin.report.model.Report;

import javassist.NotFoundException;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

/**
 * Action for supporting JSP reports
 *
 * @author Shamil Vakhitov
 */
@Action(path = "/user/plugin/report/report", pathId = true)
public class ReportAction extends BaseAction {

    @Override
    public ActionForward unspecified(DynActionForm form, ConnectionSet conSet) {
        // restrictions for JSP reports
        form.setResponseData("allowedReports", Utils.toSet(form.getPermission().get("allowedReports")));
        form.setRequestAttribute("config", setup.getConfig(Config.class));

        return html(conSet, form, Plugin.PATH_JSP_USER + "/report.jsp");
    }

    public ActionForward get(DynActionForm form, ConnectionSet conSet) throws NotFoundException {
        Report report = setup.getConfig(Config.class).getReportMap().get(form.get("reportId"));
        if (report == null)
            throw new NotFoundException("Report not found");

        // deprecated, but already used in existing reports
        form.setForwardFile(report.getJsp());

        return html(conSet, form, report.getJsp());
    }
}
