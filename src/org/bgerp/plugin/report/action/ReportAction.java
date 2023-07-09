package org.bgerp.plugin.report.action;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts.action.ActionForward;
import org.bgerp.app.bean.Bean;
import org.bgerp.plugin.report.Plugin;
import org.bgerp.plugin.report.dao.ReportDAO;
import org.bgerp.plugin.report.model.Config;
import org.bgerp.plugin.report.model.Report;

import ru.bgcrm.model.BGException;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.action.BaseAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

/**
 * Deprecated action for supporting JSP reports.
 *
 * @author Shamil Vakhitov
 */
@Action(path = "/user/plugin/report/report")
public class ReportAction extends BaseAction {

    public ActionForward get(DynActionForm form, ConnectionSet conSet) throws Exception {
        Report report = setup.getConfig(Config.class).getReportMap().get(form.get("reportId"));
        if (report == null)
            throw new BGException("Report not found");

        if (StringUtils.isNotBlank(report.getDaoClass())) {
            log.debug("Creating Java DAO class: {}", report.getDaoClass());

            ReportDAO dao = Bean.newInstance(report.getDaoClass());
            dao.get(form);

            return html(conSet, form, dao.getJspFile());
        }

        // form.setForwardFile(report.getJspFile()); // for backward compatibility
        return html(conSet, form, report.getJspFile());
    }

    @Override
    public ActionForward unspecified(DynActionForm form, ConnectionSet conSet) {
        form.getResponse().setData("allowedReports", Utils.toSet(form.getPermission().get("allowedReports")));

        return html(conSet, form, Plugin.PATH_JSP_USER + "/report.jsp");
    }
}
